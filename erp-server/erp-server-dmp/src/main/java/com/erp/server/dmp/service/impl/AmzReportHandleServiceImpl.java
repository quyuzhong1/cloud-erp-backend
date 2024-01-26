package com.erp.server.dmp.service.impl;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.dto.UniqueDto;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.erp.model.dmp.entity.AmzReportScheduleEntity;
import com.erp.model.dmp.entity.AmzReportTaskEntity;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedTypeEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.rpc.wms.feign.WmsShipmentFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.*;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentItemsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItemList;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentList;
import com.erp.sdk.oms.amz.spapi.model.reports.*;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiRateLimitUtils;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.convert.DmpFbaInventoryConverter;
import com.erp.server.dmp.convert.DmpReportConverter;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
public class AmzReportHandleServiceImpl implements AmzReportHandleService {

    @Resource
    private MongoService mongoService;
    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private AmzReportScheduleService reportScheduleService;
    @Resource
    private DmpPullTaskService dmpPullTaskService;
    @Resource
    private WmsShipmentFeign wmsShipmentFeign;
    @Resource
    private CfgAmzReportFieldService cfgAmzReportFieldService;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private AmazonSpApiRateLimitUtils amazonSpApiRateLimitUtils;
    @Resource
    private RedisUtil redisUtil;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean pullShipment(DmpPullShipmentDTO dto) {
        // 获取店铺信息
        String shopId = dto.getShopId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        try {
            FbaInboundApi api = FbaInboundApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false);
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
                    .map(e -> new PlatformAmazonFbaShipmentDTO(e, shopId, shopInfoDTO.getName()))
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

                // 合并成详情
                List<PlatformFbaShipmentReceiveDTO> detailListDTO = new ArrayList<>(
                        receiveDTOList.stream()
                                .collect(Collectors.toMap(
                                        shipment -> shipment.getFbaShipmentId() + shipment.getFnSku() + shipment.getSellerSku(),
                                        shipment -> shipment,
                                        PlatformFbaShipmentReceiveDTO::merge))
                                .values()
                );
                // 过滤为0
                List<PlatformFbaShipmentReceiveDTO> saveReceiveDTO = receiveDTOList.stream().filter(e -> e.getReceiveQty() > 0).collect(Collectors.toList());
                platformFbaShipmentDTO.setReceiveDTOList(saveReceiveDTO);

                // 检查签收时间
                detailListDTO.forEach(e-> {
                    if (e.getReceiveQty() == 0) {
                        e.setReceiveDate(null);
                    }
                });
                platformFbaShipmentDTO.setDetailList(detailListDTO);

//                businessService.pullDetailProcess(amazonShipmentDTO, platformFbaShipmentDTO, category, platform, business);

                String topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC;
                String tag = StrUtil.format("{}_{}", category, business) + "_tag";
                BusinessTypeEnum businessType = BusinessTypeEnum.getByCodeAndThrow(business);

                Class<? extends PlatformAmazonFbaShipmentDTO> tClass = amazonShipmentDTO.getClass();
                String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
                // 保存或更新mongo数据
                UniqueDto uniqueDto = UniqueDto.getUniqId(platformFbaShipmentDTO.getUniqueId());
                List<? extends PlatformAmazonFbaShipmentDTO> mongoData = mongoService.findMongoData(uniqueDto, 0, 0, tableName, tClass);
                if (CollectionUtils.isEmpty(mongoData)){
                    mongoService.saveMongoData(amazonShipmentDTO, tableName);
                } else {
                    MapUtil mapUtil =JSONObject.parseObject(JSONObject.toJSONString(amazonShipmentDTO), MapUtil.class);
                    mongoService.updateMongoData(uniqueDto, mapUtil, tableName, tClass);
                }

                String modelTaskId = dmpPullTaskService.saveOrUpdateDmpSyncTask(new DmpPullTaskEntity(platform, businessType.getSourceType().getCode(), platform, topic, tag, platformFbaShipmentDTO));
                // 同步处理
                dmpPullTaskService.updateSyncInfo(String.valueOf(modelTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");

                log.info("手动拉取货件推送：{}", JSONUtil.toJsonStr(platformFbaShipmentDTO));
                ApiResult<?> apiResult = wmsShipmentFeign.consumerPullShipment(platformFbaShipmentDTO);
                if (200 != apiResult.getCode()){
                    throw new ServiceException("拉取货件处理失败");
                }
                log.info("手动拉取货件结果：{}", JSONUtil.toJsonStr(platformFbaShipmentDTO));
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
    public void createReportSchedule(AmzReportScheduleEntity reportSchedule, OffsetDateTime currentDateTime) throws Exception {
        CreateReportScheduleSpecification.PeriodEnum periodEnum = CreateReportScheduleSpecification.PeriodEnum.getByCode(reportSchedule.getPeriod());
        // 支持切换时间间隔
        OffsetDateTime roundedOffsetDateTime = periodEnum.formatTime(currentDateTime);

        String[] marketplaceIdArray = reportSchedule.getMarketplaceIds().split(",");
        // 当前市场ID
        String marketplaceId = Stream.of(marketplaceIdArray)
                .findFirst()
                .orElseThrow(() -> new ServiceException("未找到市场信息"));
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(marketplaceId);

        // 获取店铺信息
        String shopId = reportSchedule.getShopId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }
        // 手动订单修改
        if (ReportScheduleSubscribedTypeEnum.MANUAL.getCode().equalsIgnoreCase(reportSchedule.getSubscribedType())){
            // 更新到记录
            reportSchedule.setFirstNextReportCreationTime(roundedOffsetDateTime.toLocalDateTime());
            reportSchedule.setSubscribedStatus(ReportScheduleSubscribedStatusEnum.ALREADY.getCode());
            if (!reportScheduleService.updateById(reportSchedule)) {
                throw new ServiceException("[ReportScheduleEntity] 更新亚马逊报价计划失败");
            }
            return;
        }

        // 请求参数
        CreateReportScheduleSpecification body = new CreateReportScheduleSpecification();
        body.setReportType(reportSchedule.getReportType());
        body.setMarketplaceIds(Arrays.stream(marketplaceIdArray).collect(Collectors.toList()));
        // 格式"2023-11-07T01:00:00.000Z"
        String formatTime = roundedOffsetDateTime.format(DateTimeFormatter.ofPattern(DatePattern.UTC_MS_PATTERN));
        body.setNextReportCreationTime(formatTime);
        body.setPeriod(periodEnum);
        // 请求
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO , true, null);
        // TODO 兼容已创建
        CreateReportScheduleResponse response = reportsApi.createReportSchedule(body);

        // 更新到记录
        reportSchedule.setAmzReportScheduleId(response.getReportScheduleId());
        reportSchedule.setFirstNextReportCreationTime(roundedOffsetDateTime.toLocalDateTime());
        reportSchedule.setSubscribedStatus(ReportScheduleSubscribedStatusEnum.ALREADY.getCode());
        if (!reportScheduleService.updateById(reportSchedule)) {
            throw new ServiceException("[ReportScheduleEntity] 更新亚马逊报价计划失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAmzReport(AmzReportTaskEntity taskEntity){
        // 从缓存获取(已完成或结束删除)
        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_RESULT_PREFIX, taskEntity.getId(), taskEntity.getStatus());
        Object reportIdObj = redisUtil.get(key);
        if (null != reportIdObj){
            return (String) reportIdObj;
        }
        // 校验MarketplaceId
        String[] marketplaceSplit = taskEntity.getMarketplaceIds().split(",");
        if (marketplaceSplit.length == 0) {
            throw new ServiceException("未找到MarketplaceId,reportScheduleId=" + taskEntity.getId());
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(marketplaceSplit[0]);
        if (null == marketplaceEnum) {
            String msg = StrUtil.format("未找到Marketplace枚举类型,reportScheduleId={}, marketplaceId={}", taskEntity.getId(), taskEntity.getMarketplaceIds());
            throw new ServiceException(msg);
        }
        // 获取店铺信息
        String shopId = taskEntity.getShopId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.REPORTS_CREATE;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX, taskEntity.getGroupId());
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);
        String rateLimitStr;

        // 请求亚马逊接口
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);
        CreateReportSpecification body = new CreateReportSpecification();
        body.setReportType(taskEntity.getReportType());
        body.setMarketplaceIds(Stream.of(marketplaceSplit).collect(Collectors.toList()));
        if (StringUtils.isNotBlank(taskEntity.getReqDataStartTime())){
            body.setDataStartTime(taskEntity.getReqDataStartTime());
        }
        if (StringUtils.isNotBlank(taskEntity.getReqDataEndTime())){
            body.setDataEndTime(taskEntity.getReqDataEndTime());
        }
        ApiResponse<CreateReportResponse> reportWithHttpInfo;
        try {
            reportWithHttpInfo = reportsApi.createReportWithHttpInfo(body);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        // 检查和缓存响应的速率到redis
        amazonSpApiRateLimitUtils.checkAndSetRedis(limitKey, reportWithHttpInfo);
        CreateReportResponse reportResponse = reportWithHttpInfo.getData();
        String reportId = reportResponse.getReportId();
        if (null == reportId) {
            throw new ServiceException("请求亚马逊创建报告失败：body=" + JSONUtil.toJsonStr(reportResponse));
        }
        // 设置到缓存(已完成或结束删除)
        redisUtil.set(key, reportId);
        return reportId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handlerNotifications(cn.hutool.json.JSONObject textMessageObj) throws Exception {
        log.warn("处理亚马逊报告通知：{}", JSONUtil.toJsonStr(textMessageObj));
//        if (BusinessCommonConstants.hasProfile("test")) {
            // 测试环境暂时过滤
//            return;
//        }

        NotificationSQSEntity sqsEntity = JSONUtil.toBean(textMessageObj, NotificationSQSEntity.class);
//        String processingStatus = sqsEntity.getPayload().getReportProcessingFinishedNotification().getProcessingStatus();
//        // 非已完成的报表
//        if (!"DONE".equalsIgnoreCase(processingStatus)) {
//            return;
//        }
        // 是否是需要记录的类型
        AmazonReportRecordTypeEnum recordTypeEnum = AmazonReportRecordTypeEnum.getByRecordType(sqsEntity.getPayload().getReportProcessingFinishedNotification().getReportType());
        if (null == recordTypeEnum) {
            log.info("未支持亚马逊报告通知忽略：{}", JSONUtil.toJsonStr(textMessageObj));
            return;
        }

        // 根据不同地区区分
//        ReportsApi reportsApi = ReportsApi.initApi(AmazonEndpointsEnum.US_EAST_1);
        ReportsApi reportsApi = null;
        // 查询当前报告是否是属于系统计划报告
        Report report = reportsApi.getReport(sqsEntity.getPayload().getReportProcessingFinishedNotification().getReportId());
        this.handleReport(reportsApi, report, recordTypeEnum, null);

    }

    @Override
    public void saveMongoAndHandle(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Report report, AmzReportScheduleEntity reportScheduleEntity, ReportInfoMongoDTO reportInfoMongoDTO, Map<String, String> columnMap) throws IOException {
        List<?> cvsList = handleDownloadAndParse(reportDocument, recordTypeEnum, columnMap);
        // 填充报告相关信息
        List<? extends ReportSuperMongoDTO> mongoDTOSList = handleData(cvsList, report, recordTypeEnum);

        // 报告保存
        mongoService.saveMongoData(reportInfoMongoDTO, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT);

        // 填充报告来源信息
        mongoService.saveMongoDataMult(mongoDTOSList, recordTypeEnum.getMongoTableName());

    }

    @Override
    public List<?> handleDownloadAndParse(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Map<String, String> columnMap) throws IOException {
        String compressionAlgorithm = null == reportDocument.getCompressionAlgorithm() ? "" : reportDocument.getCompressionAlgorithm().getValue();

        return AmazonSpApiReportUtils.downloadFromFastDFSAndParse(reportDocument.getUrl(), recordTypeEnum.getCvsClass(), columnMap, recordTypeEnum.getRecordType());
    }

    @Override
    public void updateMongoAndHandle(ReportDocument reportDocument, AmazonReportRecordTypeEnum recordTypeEnum, Report report, AmzReportScheduleEntity reportScheduleEntity, ReportInfoMongoDTO reportInfoMongoDTO, Map<String, String> columnMap) throws IOException {

        String compressionAlgorithm = null == reportDocument.getCompressionAlgorithm() ? "" : reportDocument.getCompressionAlgorithm().getValue();

        List<?> cvsList = AmazonSpApiReportUtils.downloadFromFastDFSAndParse(reportDocument.getUrl(), recordTypeEnum.getCvsClass(), columnMap, recordTypeEnum.getRecordType());
        // 填充报告相关信息
        List<? extends ReportSuperMongoDTO> mongoDTOSList = handleData(cvsList, report, recordTypeEnum);

        // 更新报告保存
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(reportInfoMongoDTO), MapUtil.class);
        ReportInfoMongoDTO updateDto = ReportInfoMongoDTO.getId(reportInfoMongoDTO.getId());
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);

        // 填充报告来源信息
        mongoService.saveMongoDataMult(mongoDTOSList, recordTypeEnum.getMongoTableName());

        // TODO 扩展
        if (AmazonReportRecordTypeEnum.GET_MERCHANT_LISTINGS_ALL_DATA.getRecordType().equalsIgnoreCase(report.getReportType())) {
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void saveOrUpdateAllReportFbaInventoryPlanning(ReportInfoMongoDTO mongoDTO, List<ReportFbaInventoryPlanningMongoDTO> planningMongoDTOList) {
        // 报告保存已处理
        mongoDTO.setReportHandleStatus(1);
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDTO), MapUtil.class);
        ReportInfoMongoDTO updateDto = ReportInfoMongoDTO.getId(mongoDTO.getId());
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleReport(ReportsApi reportsApi, Report report, AmazonReportRecordTypeEnum recordTypeEnum, Map<String, String> columnMap) throws Exception{
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
        AmzReportScheduleEntity reportScheduleEntity = null;
        // 报告计划任务
        if (StringUtils.isNotBlank(reportScheduleId)) {
            // 查询报告计划ID是否已存在
            reportScheduleEntity = reportScheduleService.getByReportScheduleId(reportScheduleId);
        }

        // 校验报告
        ReportInfoMongoDTO reportMongoDTO = ReportInfoMongoDTO.getReportId(report.getReportId());
        List<ReportInfoMongoDTO> mongoData = mongoService.findMongoData(reportMongoDTO, 0, 0, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);
        if (null != reportScheduleEntity) {
            // 报告计划存在, 报告记录存在忽略
            if (CollectionUtils.isEmpty(mongoData)) {
                // 查询报告当前链接
                ReportDocument reportDocument = reportsApi.getReportDocument(report.getReportDocumentId());
                // 转换
                ReportInfoMongoDTO reportInfoMongoDTO = DmpReportConverter.INSTANCE.newReportInfoMongoDTO(report, reportDocument, reportScheduleEntity);
                // 保存并处理
                this.saveMongoAndHandle(reportDocument, recordTypeEnum, report, reportScheduleEntity, reportInfoMongoDTO, columnMap);
            }
            return;
        } else {
            // 非报价计划从报告信息的主表ID 获取reportSchedule
            if (!CollectionUtils.isEmpty(mongoData)){
                ReportInfoMongoDTO oldReportInfoMongoDTO = mongoData.get(0);
                reportScheduleEntity = reportScheduleService.getById(oldReportInfoMongoDTO.getMainId());
            }
        }

        if (null == reportScheduleEntity){
            log.info("非系统请求的报告ID,忽略:ReportId={}", report.getReportId());
            return;
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
        this.updateMongoAndHandle(reportDocument, recordTypeEnum, report, reportScheduleEntity, reportInfoMongoDTO, columnMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report queryAmzReportInfo(AmzReportTaskEntity taskEntity) {
        // 店铺信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(taskEntity.getShopId());
        // 市场信息
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.REPORTS_QUERY;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, taskEntity.getGroupId(), requestTypeRateLimiterEnum.getBusinessTypeName());
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);

        // 请求亚马逊接口
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);

        ApiResponse<Report> reportWithHttpInfo;
        Report report;
        try {
            reportWithHttpInfo = reportsApi.getReportWithHttpInfo(taskEntity.getReportId());
            report = reportWithHttpInfo.getData();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        // 检查和缓存响应的速率到redis
        amazonSpApiRateLimitUtils.checkAndSetRedis(limitKey, reportWithHttpInfo);
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report directQueryAmzReportInfo(AmzReportTaskEntity taskEntity) {
        // 店铺信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(taskEntity.getShopId());
        // 市场信息
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.REPORTS;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, taskEntity.getGroupId(), requestTypeRateLimiterEnum.getBusinessTypeName());
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);

        // 请求亚马逊接口
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);

        ApiResponse<GetReportsResponse> reportsWithHttpInfo;
        Report report;
        try {
            List<String> reportTypes = Collections.singletonList(taskEntity.getReportType());
            List<String> processingStatuses = Collections.singletonList(Report.ProcessingStatusEnum.DONE.getValue());
            Integer pageSize = 1;
            String createdSince = null;
            String createdUntil = null;
            String nextToken = null;
            List<String> marketplaceIds = Collections.singletonList(marketplaceEnum.getMarketplaceId());
            ReportsApi api = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
            reportsWithHttpInfo = api.getReportsWithHttpInfo(reportTypes, processingStatuses, marketplaceIds, pageSize, createdSince, createdUntil, nextToken);
            ReportList reportList = reportsWithHttpInfo.getData().getReports();
            report = reportList.stream().findFirst().orElse(null);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        // 检查和缓存响应的速率到redis
        amazonSpApiRateLimitUtils.checkAndSetRedis(limitKey, reportsWithHttpInfo);
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDocument queryAmzReportDocument(AmzReportInfoEntity reportInfoEntity, AmzReportTaskEntity taskEntity) {
        // 店铺
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(reportInfoEntity.getShopId());
        // 市场
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 接口类型
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.REPORTS_DOCUMENT_QUERY;

        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, taskEntity.getGroupId(), requestTypeRateLimiterEnum.getBusinessTypeName());
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);

        // 请求亚马逊接口
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);

        ApiResponse<ReportDocument> respWithHttpInfo;
        ReportDocument reportDocument;
        try {
            respWithHttpInfo = reportsApi.getReportDocumentWithHttpInfo(reportInfoEntity.getReportDocumentId());
            reportDocument = respWithHttpInfo.getData();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        // 检查和缓存响应的速率到redis
        amazonSpApiRateLimitUtils.checkAndSetRedis(limitKey, respWithHttpInfo);
        return reportDocument;
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
