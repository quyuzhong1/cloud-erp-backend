package com.erp.server.dmp.service.impl;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.ReportScheduleEntity;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.*;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentItemsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItemList;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentList;
import com.erp.sdk.oms.amz.spapi.model.reports.*;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.convert.DmpFbaInventoryConverter;
import com.erp.server.dmp.convert.DmpReportConverter;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.ReportHandleService;
import com.erp.server.dmp.service.ReportScheduleService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 亚马逊报告计划表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Slf4j
@Service
public class ReportHandleServiceImpl implements ReportHandleService {

    @Resource
    private MongoService mongoService;
    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private ReportScheduleService reportScheduleService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean pullShipment(DmpPullShipmentDTO dto) {
        // 获取店铺信息
        ShopInfoEntity shop = shopInfoFeign.getShopInfoById(dto.getShopId());
        if (null == shop) {
            throw new ServiceException("未找到店铺信息:shopId=" + dto.getShopId());
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shop.getDictCountryCode());

        try {
            FbaInboundApi api = FbaInboundApi.initApi(marketplaceEnum);
            String queryType = AmazonFbaQueryTypeEnum.SHIPMENT.getCode();
            String marketplaceId = marketplaceEnum.getMarketplaceId();
            List<String> shipmentStatusList = AmazonFbaShipmentStatusEnum.getAllStatus();
            List<String> shipmentIdList = dto.getShipmentCodeList();
            // 请求亚马逊接口
            GetShipmentsResponse shipments = api.getShipments(queryType, marketplaceId, shipmentStatusList, shipmentIdList, null, null, null);

            InboundShipmentList responseList = shipments.getPayload().getShipmentData();
            if (CollectionUtils.isEmpty(shipments.getPayload().getShipmentData())) {
                throw new ServiceException(ApiError.FBA_SHIPMENT_ERROR);
            }
            // 返回下载源数据
            List<PlatformAmazonFbaShipmentDTO> amazonFbaShipmentDTOList = responseList.stream()
                    .map(e -> new PlatformAmazonFbaShipmentDTO(e, shop))
                    .collect(Collectors.toList());

            // 查询FBA货件item
            for (PlatformAmazonFbaShipmentDTO shipmentDTO : amazonFbaShipmentDTOList) {
                GetShipmentItemsResponse response = api.getShipmentItemsByShipmentId(shipmentDTO.getShipmentInfo().getShipmentId(), marketplaceEnum.getMarketplaceId());
                InboundShipmentItemList itemData = response.getPayload().getItemData();
                shipmentDTO.setDetailList(itemData);
            }

            String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
            String platform = PlatformDictEnum.AMAZON.getCode();
            String business = BusinessTypeEnum.FBA_SHIPMENT.getCode();
            for (PlatformAmazonFbaShipmentDTO amazonShipmentDTO : amazonFbaShipmentDTOList) {
                // TODO 封装?
                amazonShipmentDTO.setDownloadStatus(1);
                amazonShipmentDTO.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                // 转换
                PlatformFbaShipmentDTO platformFbaShipmentDTO = SdkFbaShipmentConverter.INSTANCE.downloadDtoToSaveDto(amazonShipmentDTO);
                InboundShipmentItemList sourceDetailList = amazonShipmentDTO.getDetailList();
                List<PlatformFbaShipmentReceiveDTO> receiveDTOList = sourceDetailList.stream()
                        .map(SdkFbaShipmentConverter.INSTANCE::receiveDtoToSaveDto)
                        .collect(Collectors.toList());
                platformFbaShipmentDTO.setReceiveDTOList(receiveDTOList);
                // 合并成详情
                List<PlatformFbaShipmentReceiveDTO> detailListDTO = new ArrayList<>(
                        receiveDTOList.stream()
                                .collect(Collectors.toMap(
                                        shipment -> shipment.getFbaShipmentId() + shipment.getFnSku() + shipment.getSellerSku(),
                                        shipment -> shipment,
                                        PlatformFbaShipmentReceiveDTO::merge))
                                .values()
                );
                platformFbaShipmentDTO.setDetailList(detailListDTO);

                businessService.pullDetailProcess(amazonShipmentDTO, platformFbaShipmentDTO, category, platform, business);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("[Amazon SP-APi] 下载FBA货件失败" + e);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReportSchedule(ReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime) throws Exception {
        CreateReportScheduleSpecification.PeriodEnum periodEnum = CreateReportScheduleSpecification.PeriodEnum.getByCode(reportSchedule.getPeriod());
        // 支持切换时间间隔
        OffsetDateTime roundedOffsetDateTime = periodEnum.formatTime(currentDateTime);

        String[] marketplaceIdArray = reportSchedule.getMarketplaceIds().split(",");
        // 当前市场ID
        String marketplaceId = Stream.of(marketplaceIdArray)
                .findFirst()
                .orElseThrow(() -> new ServiceException("未找到市场信息"));
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(marketplaceId);

        // 请求参数
        CreateReportScheduleSpecification body = new CreateReportScheduleSpecification();
        body.setReportType(reportSchedule.getReportType());
        body.setMarketplaceIds(Arrays.stream(marketplaceIdArray).collect(Collectors.toList()));
        // 格式"2023-11-07T01:00:00.000Z"
        String formatTime = roundedOffsetDateTime.format(DateTimeFormatter.ofPattern(DatePattern.UTC_MS_PATTERN));
        body.setNextReportCreationTime(formatTime);
        body.setPeriod(periodEnum);
        // 请求
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum());
        // TODO 兼容已创建
        CreateReportScheduleResponse response = reportsApi.createReportSchedule(body);

        // 更新到记录
        reportSchedule.setReportScheduleId(response.getReportScheduleId());
        reportSchedule.setFirstNextReportCreationTime(roundedOffsetDateTime.toLocalDateTime());
        reportSchedule.setSubscribedStatus(ReportScheduleSubscribedStatusEnum.ALREADY.getCode());
        if (!reportScheduleService.updateById(reportSchedule)) {
            throw new ServiceException("[ReportScheduleEntity] 更新亚马逊报价计划失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReport(ReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime) throws Exception {
        // 间隔
        Thread.sleep(20000);
        // 校验MarketplaceId
        String[] marketplaceSplit = reportSchedule.getMarketplaceIds().split(",");
        if (marketplaceSplit.length == 0) {
            throw new ServiceException("未找到MarketplaceId,reportScheduleId=" + reportSchedule.getId());
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(marketplaceSplit[0]);
        if (null == marketplaceEnum) {
            String msg = StrUtil.format("未找到Marketplace枚举类型,reportScheduleId={}, marketplaceId={}", reportSchedule.getId(), reportSchedule.getMarketplaceIds());
            throw new ServiceException(msg);
        }
        CreateReportScheduleSpecification.PeriodEnum periodEnum = CreateReportScheduleSpecification.PeriodEnum.getByCode(reportSchedule.getPeriod());
        // 支持切换时间间隔
        // 修改下次创建时间
        OffsetDateTime roundedOffsetDateTime = periodEnum.formatTime(currentDateTime);
        LocalDateTime nextTime = periodEnum.plusPeriod(roundedOffsetDateTime)
                .withOffsetSameInstant(BusinessCommonConstants.systemZoneOffset)
                .toLocalDateTime();
        reportSchedule.setFirstNextReportCreationTime(nextTime);
        if (!reportScheduleService.updateById(reportSchedule)) {
            throw new ServiceException("[reportSchedule] 更新失败");
        }

        // 请求亚马逊接口
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum());
        CreateReportSpecification body = new CreateReportSpecification();
        body.setReportType(reportSchedule.getReportType());
        body.setMarketplaceIds(Stream.of(marketplaceSplit).collect(Collectors.toList()));
        CreateReportResponse reportResponse = reportsApi.createReport(body);
        String reportId = reportResponse.getReportId();
        if (null == reportId) {
            throw new ServiceException("请求亚马逊创建报告失败：body=" + JSONUtil.toJsonStr(reportResponse));
        }
        ReportInfoMongoDTO reportInfoMongoDTO = new ReportInfoMongoDTO();
        reportInfoMongoDTO.setReportId(reportId);
        reportInfoMongoDTO.setReportType(reportSchedule.getReportType());
        reportInfoMongoDTO.setMainId(reportSchedule.getId());
        reportInfoMongoDTO.setCreatedTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        reportInfoMongoDTO.setShopId(reportSchedule.getShopId());
        // 报告保存
        mongoService.saveMongoData(reportInfoMongoDTO, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT);

    }

    @Override
    public void pullBusinessHandler(String shopId, String reportId, List<? extends ReportSuperMongoDTO> mongoDTOSList) {
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setShopId(shopId);
        jobTaskDTO.setShopName(shopId);
        jobTaskDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        jobTaskDTO.setApiCode("products");
        jobTaskDTO.setApiName("亚马逊Listing");
        jobTaskDTO.setIntervalTime(900);
        jobTaskDTO.setStatus(1);
        jobTaskDTO.setRetryTimes(0);
        jobTaskDTO.setCreateTime(LocalDateTime.now());
        jobTaskDTO.setUpdateTime(LocalDateTime.now());
        //
        jobTaskDTO.setPlatformApiId(reportId);
        jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
        jobTaskDTO.setBillType(BusinessTypeEnum.PRODUCT.getCode());
        jobTaskDTO.setOperateType("pull");
        jobTaskDTO.setMongoDataList(mongoDTOSList);
        // 事务处理
        businessService.pullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO);

    }

    @Override
    public void handlerNotifications(SQSTextMessage textMessage) throws Exception {
//        if (BusinessCommonConstants.hasProfile("test")) {
            // 测试环境暂时过滤
            log.warn("监听到亚马逊报告通知：{}", JSONUtil.toJsonStr(textMessage));
//            return;
//        }

        NotificationSQSEntity sqsEntity = JSONUtil.toBean(textMessage.getText(), NotificationSQSEntity.class);
        String processingStatus = sqsEntity.getPayload().getReportProcessingFinishedNotification().getProcessingStatus();
        // 非已完成的报表
        if (!"DONE".equalsIgnoreCase(processingStatus)) {
            return;
        }
        // 是否是需要记录的类型
        AmazonReportRecordTypeEnum recordTypeEnum = AmazonReportRecordTypeEnum.getByRecordType(sqsEntity.getPayload().getReportProcessingFinishedNotification().getReportType());
        if (null == recordTypeEnum) {
            return;
        }
        // 根据不同地区区分
        ReportsApi reportsApi = ReportsApi.initApi(AmazonEndpointsEnum.US_EAST_1);
        // 查询当前报告是否是属于系统计划报告
        Report report = reportsApi.getReport(sqsEntity.getPayload().getReportProcessingFinishedNotification().getReportId());
        if (!"DONE".equalsIgnoreCase(report.getProcessingStatus().getValue())){
            log.error("报告状态未完成：{}",JSONUtil.toJsonStr(report));
            // 未完成也更新
            ReportInfoMongoDTO reportMongoDTO = ReportInfoMongoDTO.getReportId(report.getReportId());
            List<ReportInfoMongoDTO> mongoData = mongoService.findMongoData(reportMongoDTO, 0, 0, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);
            ReportInfoMongoDTO oldReportInfoMongoDTO = mongoData.get(0);
            // 转换
            ReportInfoMongoDTO reportInfoMongoDTO = DmpReportConverter.INSTANCE.updateReportInfoMongoDTO(oldReportInfoMongoDTO, null, report);
            // 更新报告保存
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(reportInfoMongoDTO), MapUtil.class);
            ReportInfoMongoDTO updateDto = ReportInfoMongoDTO.getId(reportInfoMongoDTO.getId());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);
            return;
        }

        // 报告计划
        String reportScheduleId = report.getReportScheduleId();
        ReportScheduleEntity reportScheduleEntity = null;
        // 报告计划任务
        if (StringUtils.isNotBlank(reportScheduleId)) {
            // 查询报告计划ID是否已存在
            reportScheduleEntity = reportScheduleService.getByReportScheduleId(reportScheduleId);
        }
        
        // 校验报告
        ReportInfoMongoDTO reportMongoDTO = ReportInfoMongoDTO.getReportId(report.getReportId());
        List<ReportInfoMongoDTO> mongoData = mongoService.findMongoData(reportMongoDTO, 0, 0, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);
        // 报告计划存在, 报告记录存在忽略
        if (null != reportScheduleEntity) {
            if (!CollectionUtils.isEmpty(mongoData)){
                // 存在跳过
                return;
            } else {
                // 查询报告当前链接
                ReportDocument reportDocument = reportsApi.getReportDocument(report.getReportDocumentId());
                // 转换
                ReportInfoMongoDTO reportInfoMongoDTO = DmpReportConverter.INSTANCE.newReportInfoMongoDTO(report, reportDocument, reportScheduleEntity);
                // 保存并处理
                this.saveMongoAndHandle(reportDocument, recordTypeEnum, report, reportScheduleEntity, reportInfoMongoDTO);
                return;
            }
        }

        // 报告计划不存在, 报告记录为空忽略
        if (CollectionUtils.isEmpty(mongoData)){
            return;
        }
        ReportInfoMongoDTO oldReportInfoMongoDTO = mongoData.get(0);
        // 查询报告当前链接
        ReportDocument reportDocument = reportsApi.getReportDocument(report.getReportDocumentId());
        // 转换
        ReportInfoMongoDTO reportInfoMongoDTO = DmpReportConverter.INSTANCE.updateReportInfoMongoDTO(oldReportInfoMongoDTO, reportDocument, report);
        // 更新并处理
        this.updateMongoAndHandle(reportDocument, recordTypeEnum, report, reportScheduleEntity, reportInfoMongoDTO);
        

    }

    @Override
    public void saveMongoAndHandle(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Report report, ReportScheduleEntity reportScheduleEntity, ReportInfoMongoDTO reportInfoMongoDTO) throws IOException {
        List<?> cvsList = AmazonSpApiReportUtils.downloadAndParse(reportDocument.getUrl(), recordTypeEnum.getCvsClass());
        // 填充报告相关信息
        List<? extends ReportSuperMongoDTO> mongoDTOSList = handleData(cvsList, report, recordTypeEnum);

        // 报告保存
        mongoService.saveMongoData(reportInfoMongoDTO, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT);

        // 填充报告来源信息
        mongoService.saveMongoDataMult(mongoDTOSList, recordTypeEnum.getMongoTableName());

        // TODO 扩展
        if (AmazonReportRecordTypeEnum.GET_MERCHANT_LISTINGS_DATA.getRecordType().equalsIgnoreCase(report.getReportType())) {
            this.pullBusinessHandler(reportScheduleEntity.getShopId(), report.getReportId(), mongoDTOSList);
        }
    }

    @Override
    public void updateMongoAndHandle(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Report report, ReportScheduleEntity reportScheduleEntity, ReportInfoMongoDTO reportInfoMongoDTO) throws IOException {
        List<?> cvsList = AmazonSpApiReportUtils.downloadAndParse(reportDocument.getUrl(), recordTypeEnum.getCvsClass());
        // 填充报告相关信息
        List<? extends ReportSuperMongoDTO> mongoDTOSList = handleData(cvsList, report, recordTypeEnum);

        // 更新报告保存
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(reportInfoMongoDTO), MapUtil.class);
        ReportInfoMongoDTO updateDto = ReportInfoMongoDTO.getId(reportInfoMongoDTO.getId());
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);

        // 填充报告来源信息
        mongoService.saveMongoDataMult(mongoDTOSList, recordTypeEnum.getMongoTableName());

        // TODO 扩展
        if (AmazonReportRecordTypeEnum.GET_MERCHANT_LISTINGS_DATA.getRecordType().equalsIgnoreCase(report.getReportType())) {
            this.pullBusinessHandler(reportScheduleEntity.getShopId(), report.getReportId(), mongoDTOSList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void saveOrUpdateAllReportFbaMyiAllInventory(ReportInfoMongoDTO mongoDTO, List<ReportFbaMyiAllInventoryMongoDTO> fbaMyiAllInventoryMongoDTOList) {
        // 报告保存已处理
        mongoDTO.setReportHandleStatus(1);
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDTO), MapUtil.class);
        ReportInfoMongoDTO updateDto = ReportInfoMongoDTO.getId(mongoDTO.getId());
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);

        // TODO 优化效率
        // 查询当前店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(mongoDTO.getShopId());
        // 查询SKU绑定的信息
        List<String> sellerSkuList = fbaMyiAllInventoryMongoDTOList
                .stream()
                .map(ReportFbaMyiAllInventoryMongoDTO::getSku)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoMap;
        if (!CollectionUtils.isEmpty(sellerSkuList)) {
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTO.setPlatformSkuNoList(sellerSkuList);
            paramDTO.setMatchResult(true);
            listingInfoMap = omsListingInfoFeign.listingInfoWithSkuMappingList(paramDTO)
                    .stream()
                    .collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo, Function.identity()));
        } else {
            listingInfoMap = new HashMap<>();
        }

        // 转换实体
        List<FbaInventoryEntity> fbaInventoryEntityList = fbaMyiAllInventoryMongoDTOList
                .stream()
                .map(e -> DmpFbaInventoryConverter.INSTANCE.reportFbaMyiAllInventoryToEntity(e,
                        shopInfoEntity,
                        listingInfoMap.get(e.getSku())))
                .collect(Collectors.toList());

        // 保存到WMS
        wmsFbaInventoryFeign.allBatchSave(fbaInventoryEntityList);

    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void saveOrUpdateAllReportReserved(ReportInfoMongoDTO mongoDTO, List<ReportReservedMongoDTO> reportReservedMongoDTOList) {
        // 报告保存已处理
        mongoDTO.setReportHandleStatus(1);
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDTO), MapUtil.class);
        ReportInfoMongoDTO updateDto = ReportInfoMongoDTO.getId(mongoDTO.getId());
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);

        // TODO 优化效率
        // 查询当前店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(mongoDTO.getShopId());

        // 查询SKU绑定的信息
        List<String> sellerSkuList = reportReservedMongoDTOList
                .stream()
                .map(ReportReservedMongoDTO::getSku)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoMap;
        if (!CollectionUtils.isEmpty(sellerSkuList)) {
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTO.setPlatformSkuNoList(sellerSkuList);
            paramDTO.setMatchResult(true);
            listingInfoMap = omsListingInfoFeign.listingInfoWithSkuMappingList(paramDTO)
                    .stream()
                    .collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo, Function.identity()));
        } else {
            listingInfoMap = new HashMap<>();
        }

        // 转换实体
        List<FbaInventoryEntity> fbaInventoryEntityList = reportReservedMongoDTOList
                .stream()
                .map(e -> DmpFbaInventoryConverter.INSTANCE.reportReservedToEntity(e,
                        shopInfoEntity,
                        listingInfoMap.get(e.getSku())))
                .collect(Collectors.toList());

        // 保存到WMS
        wmsFbaInventoryFeign.allBatchSave(fbaInventoryEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void saveOrUpdateAllReportFbaInventoryPlanning(ReportInfoMongoDTO mongoDTO, List<ReportFbaInventoryPlanningMongoDTO> planningMongoDTOList) {
        // 报告保存已处理
        mongoDTO.setReportHandleStatus(1);
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDTO), MapUtil.class);
        ReportInfoMongoDTO updateDto = ReportInfoMongoDTO.getId(mongoDTO.getId());
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);

        // TODO 优化效率
        // 查询当前店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(mongoDTO.getShopId());
        // 查询SKU绑定的信息
        List<String> sellerSkuList = planningMongoDTOList
                .stream()
                .map(ReportFbaInventoryPlanningMongoDTO::getSku)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoMap;
        if (!CollectionUtils.isEmpty(sellerSkuList)) {
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTO.setPlatformSkuNoList(sellerSkuList);
            paramDTO.setMatchResult(true);
            listingInfoMap = omsListingInfoFeign.listingInfoWithSkuMappingList(paramDTO)
                    .stream()
                    .collect(Collectors.toMap(ListingInfoWithSkuMappingDTO::getPlatformSkuNo, Function.identity()));
        } else {
            listingInfoMap = new HashMap<>();
        }

        // 转换实体
        List<FbaInventoryEntity> fbaInventoryEntityList = planningMongoDTOList
                .stream()
                .map(e -> DmpFbaInventoryConverter.INSTANCE.reportFbaInventoryPlanningToEntity(e,
                        shopInfoEntity,
                        listingInfoMap.get(e.getSku())))
                .collect(Collectors.toList());

        // 保存到WMS
        wmsFbaInventoryFeign.allBatchSave(fbaInventoryEntityList);
    }



    public List<? extends ReportSuperMongoDTO> handleData(List<?> cvsList, Report report, AmazonReportRecordTypeEnum recordTypeEnum) {
        return cvsList.stream().map(o -> {
            try {
                ReportSuperMongoDTO mongoDTO = (ReportSuperMongoDTO) (recordTypeEnum.getMongoDTOClass().newInstance());
                BeanUtils.copyProperties(o, mongoDTO);
                mongoDTO.setDataStartTime(report.getDataStartTime().toString());
                mongoDTO.setDataEndTime(report.getDataEndTime().toString());
                mongoDTO.setMarketplaceIds(report.getMarketplaceIds());
                mongoDTO.setReportId(report.getReportId());
                mongoDTO.setReportScheduleId(report.getReportScheduleId());
                return mongoDTO;
            } catch (Exception e) {
                throw new ServiceException("csv转换mongoDTO失败, error=" + e.getMessage());
            }
        }).collect(Collectors.toList());
    }
}
