package com.erp.server.dmp.service.impl;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.dto.UniqueDto;
import com.common.business.enums.*;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.AmazonCreateReportResultDTO;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedTypeEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.ShopPlatformStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.rpc.wms.feign.WmsShipmentFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.api.SellersApi;
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
import com.erp.sdk.oms.amz.spapi.model.sellers.GetMarketplaceParticipationsResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiRateLimitUtils;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    private CfgAmzReportTypeService cfgAmzReportTypeService;
    @Resource
    private DmpInputCreateFactory dmpInputCreateFactory;
    @Resource
    private DmpCfgInputService dmpCfgInputService;
    @Resource
    private DmpCfgInputDetailService dmpCfgInputDetailService;
    @Resource
    private CfgSettingService cfgSettingService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean pullShipment(DmpPullShipmentDTO dto) {
        if (CollectionUtils.isEmpty(dto.getShipmentCodeList())){
            return true;
        }

        // 获取店铺信息
        String shopId = dto.getShopId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        // 查询拉取配置
        Integer count = cfgSettingService.lambdaQuery()
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.FBA_SHIPMENT.getCode())
                .eq(CfgSettingEntity::getType, SettingEnum.NEW_DMP_PULL_SWITCH_LIST.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .count();
        if (count > 0){
            // 执行新中台拉取逻辑
            return newDmpPullShipment(dto, shopInfoDTO);
        } else {
            // 执行历史逻辑
            return oldDmpPullShipment(dto, shopInfoDTO, shopId);
        }
    }

    /**
     * 历史拉取逻辑
     */
    private boolean oldDmpPullShipment(DmpPullShipmentDTO dto, AmazonShopInfoDTO shopInfoDTO, String shopId) {
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        try {
            FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
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
                detailListDTO.forEach(e -> {
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
                if (CollectionUtils.isEmpty(mongoData)) {
                    mongoService.saveMongoData(amazonShipmentDTO, tableName);
                } else {
                    MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(amazonShipmentDTO), MapUtil.class);
                    mongoService.updateMongoData(uniqueDto, mapUtil, tableName, tClass);
                }

                String modelTaskId = dmpPullTaskService.saveOrUpdateDmpSyncTask(new DmpPullTaskEntity(platform, businessType.getSourceType().getCode(), platform, topic, tag, platformFbaShipmentDTO));
                // 同步处理
                dmpPullTaskService.updateSyncInfo(String.valueOf(modelTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");

                log.info("手动拉取货件推送：{}", JSONUtil.toJsonStr(platformFbaShipmentDTO));
                ApiResult<?> apiResult = wmsShipmentFeign.consumerPullShipment(platformFbaShipmentDTO);
                if (200 != apiResult.getCode()) {
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
        if (ReportScheduleSubscribedTypeEnum.MANUAL.getCode().equalsIgnoreCase(reportSchedule.getSubscribedType())) {
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
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, true, null);
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
    public AmazonCreateReportResultDTO checkAndCreateAmzReport(AmzReportTaskEntity taskEntity, String reportGroup) {
        // 从缓存获取(已完成或结束删除)
        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_RESULT_PREFIX, taskEntity.getId(), taskEntity.getStatus());
        Object reportIdObj = redisUtil.get(key);
        if (null != reportIdObj) {
            String reportId = (String) reportIdObj;
            return AmazonCreateReportResultDTO.success(reportId);
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
        // 获取同组报告类型配置
        Map<String, List<CfgAmzReportTypeEntity>> reportMap = cfgAmzReportTypeService.mapByReportGroup();
        List<CfgAmzReportTypeEntity> groupReportList = reportMap.get(reportGroup);
        List<String> reportTypes = groupReportList.stream().map(CfgAmzReportTypeEntity::getReportType).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(reportTypes)) {
            throw new ServiceException("未找到同组的报告类型, reportGroup = " + reportGroup);
        }

        // 获取店铺信息
        String shopId = taskEntity.getShopId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

        // 查询是否有处理中的报告(响应预估处理结束时间:0=无处理中报告)
        Long estimatedWaitSecond = queryProcessReportWaitTime(reportsApi, reportTypes, taskEntity.getId(), taskEntity.getGroupId());
        if (estimatedWaitSecond > 0) {
            return AmazonCreateReportResultDTO.wait(estimatedWaitSecond);
        }

        CreateReportSpecification body = new CreateReportSpecification();
        body.setReportType(taskEntity.getReportType());
        body.setMarketplaceIds(Stream.of(marketplaceSplit).collect(Collectors.toList()));
        if (StringUtils.isNotBlank(taskEntity.getReqDataStartTime())) {
            body.setDataStartTime(taskEntity.getReqDataStartTime());
        }
        if (StringUtils.isNotBlank(taskEntity.getReqDataEndTime())) {
            body.setDataEndTime(taskEntity.getReqDataEndTime());
        }
        ApiResponse<CreateReportResponse> reportWithHttpInfo;
        try {
            // 请求亚马逊创建报告接口
            reportWithHttpInfo = reportsApi.createReportWithHttpInfo(body);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        CreateReportResponse reportResponse = reportWithHttpInfo.getData();
        String reportId = reportResponse.getReportId();
        if (null == reportId) {
            throw new ServiceException("请求亚马逊创建报告失败：body=" + JSONUtil.toJsonStr(reportResponse));
        }
        // 设置到缓存(已完成或结束删除)
        redisUtil.set(key, reportId);
        return AmazonCreateReportResultDTO.success(reportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
//        this.handleReport(reportsApi, report, recordTypeEnum, null);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report queryAmzReportInfo(AmzReportTaskEntity taskEntity) {
        // 从缓存获取(已完成或结束删除)
        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_INFO_PREFIX, taskEntity.getId(), taskEntity.getStatus());
        Object reportObj = redisUtil.get(key);
        if (null != reportObj) {
            return JSONUtil.toBean(reportObj.toString(), Report.class);
        }
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
        Report.ProcessingStatusEnum processingStatus = report.getProcessingStatus();

        if (Report.ProcessingStatusEnum.DONE.equals(processingStatus)) {
            // 设置到缓存(已完成或结束删除)
            redisUtil.set(key, JSONUtil.toJsonStr(report), 36000);
        }
        // 检查和缓存响应的速率到redis
        amazonSpApiRateLimitUtils.checkAndSetRedis(limitKey, reportWithHttpInfo);
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report directQueryAmzReportInfo(AmzReportTaskEntity taskEntity) {
        // 从缓存获取(已完成或结束删除)
        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_INFO_PREFIX, taskEntity.getId(), taskEntity.getStatus());
        Object reportObj = redisUtil.get(key);
        if (null != reportObj) {
            return JSONUtil.toBean(reportObj.toString(), Report.class);
        }
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
            reportsWithHttpInfo = reportsApi.getReportsWithHttpInfo(reportTypes, processingStatuses, marketplaceIds, pageSize, createdSince, createdUntil, nextToken);
            ReportList reportList = reportsWithHttpInfo.getData().getReports();
            report = reportList.stream().findFirst().orElse(null);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        if (null != report) {
            // 设置到缓存(已完成或结束删除)
            redisUtil.set(key, JSONUtil.toJsonStr(report), 1800);
        }

        // 检查和缓存响应的速率到redis
        amazonSpApiRateLimitUtils.checkAndSetRedis(limitKey, reportsWithHttpInfo);
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDocument queryAmzReportDocument(String shopId, String reportDocumentId, String taskId, String taskStatus){
        // 从缓存获取(已完成或结束删除)
        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_INFO_PREFIX, taskId, taskStatus);
        Object reportDocumentObj = redisUtil.get(key);
        if (null != reportDocumentObj) {
            return JSONUtil.toBean(reportDocumentObj.toString(), ReportDocument.class);
        }

        // 店铺
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 市场
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 接口类型
//        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.REPORTS_DOCUMENT_QUERY;

        // 默认请求速率配置
//        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, taskEntity.getGroupId(), requestTypeRateLimiterEnum.getBusinessTypeName());
//        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);

        // 请求亚马逊接口
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

        ApiResponse<ReportDocument> respWithHttpInfo;
        ReportDocument reportDocument;
        try {
            respWithHttpInfo = reportsApi.getReportDocumentWithHttpInfo(reportDocumentId);
            reportDocument = respWithHttpInfo.getData();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        if (null != reportDocument) {
            List<String> xAmzExpiresValues = UriComponentsBuilder.fromHttpUrl(reportDocument.getUrl()).build().getQueryParams().get("X-Amz-Expires");
            // 提取过期时间的值
            int xAmzExpires = Integer.parseInt(xAmzExpiresValues.stream().findFirst().orElse("300")) - 1;
            // 设置到缓存(已完成或结束删除)
            redisUtil.set(key, JSONUtil.toJsonStr(reportDocument), xAmzExpires);
        }
        // 检查和缓存响应的速率到redis
//        amazonSpApiRateLimitUtils.checkAndSetRedis(limitKey, respWithHttpInfo);
        return reportDocument;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void checkAndUpdateShop(List<ShopInfoEntity> shopList) throws Exception {
        if (CollectionUtils.isEmpty(shopList)) {
            return;
        }
        ShopInfoEntity currentShop = shopList.get(0);
        String shopId = currentShop.getId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 查询账户市场信息
        SellersApi sellersApi = SellersApi.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false);

        GetMarketplaceParticipationsResponse response = sellersApi.getMarketplaceParticipations();
        // 过滤得到已开启的市场
        List<String> existCountryCodeList = response.getPayload().stream()
                .filter(e -> e.getParticipation().isIsParticipating() && !e.getParticipation().isHasSuspendedListings())
                .map(e -> e.getMarketplace().getCountryCode())
                .collect(Collectors.toList());
        // 需要开启的店铺
//      List<ShopInfoEntity> openShopList = new LinkedList<>();
        // 需要关闭的店铺
        List<ShopInfoEntity> closedShopList = new LinkedList<>();

        if (CollectionUtils.isEmpty(existCountryCodeList)) {
            // 关闭所有
            closedShopList.addAll(shopList);
        } else {
            // 需要关闭的店铺
            List<ShopInfoEntity> needClosedList = shopList.stream()
                    .filter(e -> !existCountryCodeList.contains(e.getDictCountryCode()) && !ShopPlatformStatusEnum.CLOSED.getCode().equalsIgnoreCase(e.getPlatformStatus()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(needClosedList)) {
                closedShopList.addAll(needClosedList);
            }
        }

        // 取消店铺处理
        if (!CollectionUtils.isEmpty(closedShopList)) {
            List<String> shopIds = closedShopList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            XxlJobHelper.log("[检查亚马逊店铺/市场任务] 关闭不存在的店铺：{}", shopIds);
            for (ShopInfoEntity shopInfo : closedShopList) {
                platformApiTaskService.checkAndClosedPlatformShop(shopInfo);
            }
        }
    }

    @Override
    public Long queryProcessReportWaitTime(ReportsApi reportsApi, List<String> reportTypes, String taskId, String groupId) {
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_GROUP_ID_PREFIX, groupId, AmazonRequestTypeRateLimiterEnum.REPORTS.getBusinessTypeName());
        // 获取动态速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj){
            log.warn("【查询报告】 taskId={}, groupId={},存在429等待恢复:放弃当前请求任务", taskId, groupId);
            return redisUtil.getExpire(limitKey);
        }

        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.REPORTS.getRateLimit();
        try {
            // 请求亚马逊接口是否有创建中报告
            ApiResponse<GetReportsResponse> reportsWithHttpInfo = reportsApi.getReportsWithHttpInfo(
                    reportTypes,
                    Collections.singletonList(Report.ProcessingStatusEnum.IN_PROGRESS.getValue()),
                    null, 1, null, null, null);
            ReportList reportList = reportsWithHttpInfo.getData().getReports();
            if (CollectionUtils.isEmpty(reportList)) {
                // 无创建中的报告
                return 0L;
            }
            // 处理中的报告信息
            Report processReport = reportList.get(0);

            // 查询历史已完成的任务预估完成时间
            List<String> progressReportType = reportList.stream().map(Report::getReportType).distinct().collect(Collectors.toList());
            ApiResponse<GetReportsResponse> doneReportsWithHttpInfo = reportsApi.getReportsWithHttpInfo(progressReportType,
                    Collections.singletonList(Report.ProcessingStatusEnum.DONE.getValue()),
                    null, 20, null, null, null);
            ReportList doneReportList = doneReportsWithHttpInfo.getData().getReports();

            // 计算预估等待时间
            return processReport.estimatedWaitTimeWithDoneReport(doneReportList);
        } catch (Exception e) {
            if (e instanceof ApiException){
                ApiException error = (ApiException) e;
                if (429 == error.getCode()) {
                    // 设置动态速率，失效时间=1/limit
                    BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                    redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                    // 按恢复时间响应
                    return timeOut.longValue();
                }
            }
            throw new RuntimeException("请求亚马逊SP-APi报告信息异常,error=" + JSONUtil.toJsonStr(e));
        }
    }

    /**
     * 新中台拉取逻辑
     */
    public boolean newDmpPullShipment(DmpPullShipmentDTO dto, AmazonShopInfoDTO shopInfoDTO) {
        // 当前账号所有店铺ID
        List<String> sameAccountShopIds = shopInfoDTO.getMarketplaceShopIdMap().values()
                .stream()
                .map(AmazonShopInfoDTO.ShopNameDTO::getShopId)
                .distinct()
                .collect(Collectors.toList());
        // 校验新中台明细配置
        DmpCfgInputEntity inputEntity = dmpCfgInputService.lambdaQuery()
                .eq(DmpCfgInputEntity::getCode, BusinessTypeEnum.FBA_SHIPMENT.getCode())
                .eq(DmpCfgInputEntity::getDisabled, false)
                .last(" LIMIT 1 ")
                .one();
        if (null == inputEntity) {
            ServiceException.runError("FBA查询配置不存在");
        }
        List<DmpCfgInputDetailEntity> list = dmpCfgInputDetailService.lambdaQuery()
                .eq(DmpCfgInputDetailEntity::getMainId, inputEntity.getId())
                .in(DmpCfgInputDetailEntity::getNextLevelId, sameAccountShopIds)
                .list();
        if (CollectionUtils.isEmpty(list)){
            ServiceException.runError("FBA查询配置明细不存在");
        }
        List<String> inputDetailIds = list.stream().map(BaseEntity::getId).collect(Collectors.toList());

        // 创建新中台hotfix任务
        DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
        dmpInputHotfixCreateRequest.setCfgInputDetailIdList(inputDetailIds);
        dmpInputHotfixCreateRequest.setCfgInputId(inputEntity.getId());
        dmpInputHotfixCreateRequest.setDetailExtendJson(JSON.toJSONString(dto));
        dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
        return true;
    }
}
