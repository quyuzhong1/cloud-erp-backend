package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.*;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.ReportScheduleEntity;
import com.erp.model.dmp.enums.ReportScheduleCancelStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedTypeEnum;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonFbaShipmentHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonListingHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonOrderHandler;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItemList;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportList;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.erp.server.dmp.service.ReportHandleService;
import com.erp.server.dmp.service.ReportScheduleService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableScheduling
public class PullAmazonJob {

    @Resource
    private PlatformDataThread platformDataThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private MongoService mongoService;

    @Resource
    private BusinessServiceImpl businessService;

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private AmazonOrderHandler amazonOrderHandler;

    @Resource
    private AmazonListingHandler amazonListingHandler;

    @Resource
    private AmazonFbaShipmentHandler amazonFbaShipmentHandler;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private ReportHandleService reportHandleService;

    @Resource
    private ReportScheduleService reportScheduleService;

    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;

    /**
     * 拉取亚马逊任务
     */
    @XxlJob("amazonExecute")
    public ReturnT<String> execute() {
        XxlJobHelper.log("[拉取亚马逊任务] 任务开始 =====");
        threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(PlatformDictEnum.AMAZON.getCode());
        });
        XxlJobHelper.log("[拉取亚马逊任务] 任务结束 =====");
        return ReturnT.SUCCESS;
    }

    /**
     * 拉取亚马逊报表任务
     */
//    @XxlJob("amazonReportDownload")
    public ReturnT<String> reportExecute() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime yesterday = now.minusDays(1);
        // 报表处理的开始时间
        String createdSince = yesterday.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            createdSince = jobParam.getString("createdSince");
        }
        XxlJobHelper.log("[拉取亚马逊报表任务] 任务开始：报表处理的开始时间={}", createdSince);
        // 查询以存在的店铺
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.list().getData();
        if (CollectionUtil.isEmpty(shopInfoEntityList)) {
            XxlJobHelper.log("[拉取亚马逊报表任务] 任务结束：店铺列表为空");
            return ReturnT.SUCCESS;
        }
        List<AmazonMarketplaceEnum> marketplaceEnumList = shopInfoEntityList.stream()
                .filter(e -> PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(e.getDictPlatform())
                        && AuthStatusEnum.ALREADY.getCode().equalsIgnoreCase(e.getAuthStatus()))
                .map(e -> AmazonMarketplaceEnum.getByCountryCode(e.getCountryName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(marketplaceEnumList)) {
            XxlJobHelper.log("[拉取亚马逊报表任务] 任务结束：亚马逊已授权店铺列表为空");
            return ReturnT.SUCCESS;
        }

        String finalCreatedSince = createdSince;
        marketplaceEnumList.forEach(marketplaceEnum -> {
            try {
                // 查询当前marketplace的前一天到今日的所有报表
                List<String> reportTypes = Collections.singletonList(AmazonReportRecordTypeEnum.GET_MERCHANT_LISTINGS_DATA.getRecordType());
                List<String> processingStatuses = null;
                List<String> marketplaceIds = null;
                Integer pageSize = null;
                String createdUntil = null;
                String nextToken = null;
                ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum());
                ReportList allReports = reportsApi.getAllReports(reportTypes, processingStatuses, marketplaceIds, pageSize, finalCreatedSince, createdUntil, nextToken);
                platformDataThread.checkAndSaveMongo(allReports, marketplaceEnum.getMarketplaceId());
            } catch (Exception e) {
                String errorMsg = JSONUtil.toJsonStr(e);
                XxlJobHelper.log("[拉取亚马逊报表任务] 拉取亚马逊报表失败：marketplaceId={}, error={}",
                        marketplaceEnum.getMarketplaceId(),
                        errorMsg
                );
                throw new ServiceException("拉取亚马逊报表失败:error=" + errorMsg);
            }
        });
        XxlJobHelper.log("[拉取亚马逊报表任务] 任务结束");
        return ReturnT.SUCCESS;
    }


    /**
     * 拉取亚马逊订单详情任务
     */
    @XxlJob("amazonSalesOrderDetailDownload")
    public ReturnT<String> amazonSalesOrderDetail() {
        Integer size = 100;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务开始,size={}", size);
        // 根据状态查询未下载数据
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByDownloadStatus(0);
        List<PlatformAmazonOrderDTO> orderEntityList = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER, PlatformAmazonOrderDTO.class);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务结束,无需要更新的信息");
            return ReturnT.SUCCESS;
        }
        orderEntityList.forEach(dto -> {
            try {
                // 下载和处理详情
                PlatformAmazonOrderDTO newDto = amazonOrderHandler.downloadDetail(dto, null);
                String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
                String platform = PlatformDictEnum.AMAZON.getCode();
                String business = BusinessTypeEnum.ORDER.getCode();
                newDto.setDownloadStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                List<PlatformOrderDTO> convertDto = amazonOrderHandler.convert(Arrays.asList(newDto));
                // TODO
                businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
            } catch (Exception e) {
                XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail下载失败，uniqueId={}, error={}",
                        dto.getUniqueId(),
                        e.getMessage());
            }
        });
        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务结束");
        return ReturnT.SUCCESS;
    }


    /**
     * 拉取亚马逊商品详情任务
     */
    @XxlJob("amazonProductDetailDownload")
    public ReturnT<String> amazonProductDetail() {
        Integer size = 100;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务开始,size={}", size);
        // 根据状态查询未下载数据
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByDownloadStatus(0);
        List<PlatformAmazonListingDTO> list = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.THIRD_SYSTEM_AMAZON_PRODUCT, PlatformAmazonListingDTO.class);
        if (CollectionUtil.isEmpty(list)) {
            XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务结束,无需要更新的信息");
            return ReturnT.SUCCESS;
        }
        // 查询关联的FNSKU
        List<String> sellerSkuList = list.stream()
                .map(PlatformAmazonListingDTO::getSellerSku)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<FbaInventoryEntity> fbaInventoryEntityList =  wmsFbaInventoryFeign.findList(sellerSkuList);
        // TODO msku绑定的fnSku是否唯一?
        Map<String, List<FbaInventoryEntity>> fnSkuRelationMap = fbaInventoryEntityList
                .stream()
                .collect(Collectors.groupingBy(FbaInventoryEntity::getMsku));

        list.forEach(dto -> {
            try {
                // 关联FNSKU信息
                FbaInventoryEntity fbaInventoryEntity = fnSkuRelationMap.getOrDefault(dto.getSellerSku(), Collections.emptyList())
                        .stream()
                        .findFirst()
                        .orElse(null);

                // 下载和处理详情
                PlatformAmazonListingDTO newDto = amazonListingHandler.downloadDetail(dto, new JSONObject());
                String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
                String platform = PlatformDictEnum.AMAZON.getCode();
                String business = BusinessTypeEnum.PRODUCT.getCode();
                List<PlatformProductDTO> convertDto = amazonListingHandler.convert(Collections.singletonList(newDto));
                newDto.setPlatformFnSku(null == fbaInventoryEntity ? "" : fbaInventoryEntity.getFnSku());
                newDto.setDownloadStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
            } catch (Exception e) {
                XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail，uniqueId={}, error={}",
                        dto.getUniqueId(),
                        e.getMessage());
            }
        });
        XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务结束");
        return ReturnT.SUCCESS;
    }


    /**
     * 拉取亚马逊Fba货件详情任务
     */
    @XxlJob("amazonFbaShipmentDetailDownload")
    public ReturnT<String> amazonFbaShipmentDetail() {
        Integer size = 100;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 任务开始,size={}", size);
        // 根据状态查询未下载数据
        PlatformAmazonFbaShipmentDTO fbaShipmentDTO = PlatformAmazonFbaShipmentDTO.getByDownloadStatus(0);
        List<PlatformAmazonFbaShipmentDTO> entityList = mongoService.findMongoData(fbaShipmentDTO, 1, size, MongoTableNameContant.THIRD_SYSTEM_AMAZON_FBA_SHIPMENT, PlatformAmazonFbaShipmentDTO.class);
        if (CollectionUtil.isEmpty(entityList)) {
            XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 任务结束,无需要更新的信息");
            return ReturnT.SUCCESS;
        }
        XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 下载开始");
        entityList.forEach(dto -> {
            try {
                // 下载和处理详情
                PlatformAmazonFbaShipmentDTO newDto;
                // 非线上支持手动
                if (!BusinessCommonConstants.hasProfile("prod") && dto.getUniqueId().contains("手动测试")) {
                    newDto = dto;
                } else {
                    newDto = amazonFbaShipmentHandler.downloadDetail(dto, null);
                }
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 下载完：{}", JSONUtil.toJsonStr(dto));
                String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
                String platform = PlatformDictEnum.AMAZON.getCode();
                String business = BusinessTypeEnum.FBA_SHIPMENT.getCode();
                newDto.setDownloadStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 主体转换前：{}", JSONUtil.toJsonStr(newDto));
                // 转换
                PlatformFbaShipmentDTO shipmentDTO = SdkFbaShipmentConverter.INSTANCE.downloadDtoToSaveDto(newDto);
                InboundShipmentItemList sourceDetailList = newDto.getDetailList();
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 详情转换前：{}", JSONUtil.toJsonStr(newDto));
                List<PlatformFbaShipmentReceiveDTO> receiveDTOList = sourceDetailList.stream()
                        .map(SdkFbaShipmentConverter.INSTANCE::receiveDtoToSaveDto)
                        .collect(Collectors.toList());
                shipmentDTO.setReceiveDTOList(receiveDTOList);
                // 合并成详情
                List<PlatformFbaShipmentReceiveDTO> detailListDTO = new ArrayList<>(
                        receiveDTOList.stream()
                                .collect(Collectors.toMap(
                                        shipment -> shipment.getFbaShipmentId() + shipment.getFnSku() + shipment.getSellerSku(),
                                        shipment -> shipment,
                                        PlatformFbaShipmentReceiveDTO::merge))
                                .values()
                );
                shipmentDTO.setDetailList(detailListDTO);
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 推送前：{}", JSONUtil.toJsonStr(newDto));
                businessService.pullDetailProcess(newDto, shipmentDTO, category, platform, business);
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 推送后：{}", JSONUtil.toJsonStr(newDto));
            } catch (Exception e) {
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload下载失败，uniqueId={}, error={}",
                        dto.getUniqueId(),
                        e.getMessage());
            }
        });
        XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 亚马逊组合库存信息任务
     */
    @XxlJob("amazonFbaInventoryCombineJob")
    public ReturnT<String> amazonFbaInventoryJob() {
        Integer size = 1;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        XxlJobHelper.log("[亚马逊组合库存信息任务] 任务开始 size={}", size);

        // 查询库存管理报告
        ReportInfoMongoDTO queryDTO = ReportInfoMongoDTO.reportHandleStatusAndDone();

        List<ReportInfoMongoDTO> mongoData = mongoService.findMongoData(queryDTO, 0, size, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);
        if (CollectionUtils.isEmpty(mongoData)) {
            XxlJobHelper.log("[亚马逊组合库存信息任务] 任务结束,无需要组合的库存主记录");
            return ReturnT.SUCCESS;
        }
        // 过滤非库存报告
        List<ReportInfoMongoDTO> invertoryMongoDTOList = mongoData.stream().filter(
                e -> AmazonReportRecordTypeEnum.getInventoryReportList().contains(e.getReportType())
        ).collect(Collectors.toList());

        invertoryMongoDTOList.forEach(mongoDTO -> {
            try {
                if (AmazonReportRecordTypeEnum.GET_FBA_MYI_ALL_INVENTORY_DATA.getRecordType().equalsIgnoreCase(mongoDTO.getReportType())) {
                    // 库存管理数据
                    ReportFbaMyiAllInventoryMongoDTO queryMyiAllInventoryMongoDTO = ReportFbaMyiAllInventoryMongoDTO.queryReportId(mongoDTO.getReportId());
                    List<ReportFbaMyiAllInventoryMongoDTO> fbaMyiAllInventoryMongoDTOList = mongoService.findMongoData(queryMyiAllInventoryMongoDTO, 0, 0, MongoTableNameContant.REPORT_AMAZON_FBA_MYI_ALL_INVENTORY, ReportFbaMyiAllInventoryMongoDTO.class);
                    if (CollectionUtils.isEmpty(fbaMyiAllInventoryMongoDTOList)) {
                        XxlJobHelper.log("[亚马逊组合库存信息任务] 库存管理数据为空异常,ReportFbaMyiAllInventoryMongoDTO={}", JSONUtil.toJsonStr(fbaMyiAllInventoryMongoDTOList));
                        return;
                    }
                    // 保存或更新
                    reportHandleService.saveOrUpdateAllReportFbaMyiAllInventory(mongoDTO, fbaMyiAllInventoryMongoDTOList);
                }
                if (AmazonReportRecordTypeEnum.GET_RESERVED_INVENTORY_DATA.getRecordType().equalsIgnoreCase(mongoDTO.getReportType())) {
                    // 库存预留数据
                    ReportReservedMongoDTO queryReservedMongoDTO = ReportReservedMongoDTO.queryReportId(mongoDTO.getReportId());
                    List<ReportReservedMongoDTO> reportReservedMongoDTOList = mongoService.findMongoData(queryReservedMongoDTO, 0, 0, MongoTableNameContant.REPORT_AMAZON_RESERVED, ReportReservedMongoDTO.class);
                    if (CollectionUtils.isEmpty(reportReservedMongoDTOList)) {
                        XxlJobHelper.log("[亚马逊组合库存信息任务] 库存预留数据为空异常,ReportReservedMongoDTO={}", JSONUtil.toJsonStr(reportReservedMongoDTOList));
                        return;
                    }
                    // 保存或更新
                    reportHandleService.saveOrUpdateAllReportReserved(mongoDTO, reportReservedMongoDTOList);
                }

                if (AmazonReportRecordTypeEnum.GET_RESERVED_INVENTORY_DATA.getRecordType().equalsIgnoreCase(mongoDTO.getReportType())) {
                    // 库存状况数据
                    ReportFbaInventoryPlanningMongoDTO queryPlanningMongoDTO = ReportFbaInventoryPlanningMongoDTO.queryReportId(mongoDTO.getReportId());
                    List<ReportFbaInventoryPlanningMongoDTO> planningMongoDTOList = mongoService.findMongoData(queryPlanningMongoDTO, 0, 0, MongoTableNameContant.REPORT_AMAZON_FBA_INVENTORY_PLANNING, ReportFbaInventoryPlanningMongoDTO.class);
                    if (CollectionUtils.isEmpty(planningMongoDTOList)) {
                        XxlJobHelper.log("[亚马逊组合库存信息任务] 库存状况数据为空异常,ReportFbaInventoryPlanningMongoDTO={}", JSONUtil.toJsonStr(planningMongoDTOList));
                        return;
                    }
                    // 保存或更新
                    reportHandleService.saveOrUpdateAllReportFbaInventoryPlanning(mongoDTO, planningMongoDTOList);
                }
            } catch (Exception e) {
                String jsonStr = JSONUtil.toJsonStr(mongoDTO);
                log.error("[亚马逊组合库存信息任务] 保存或更新异常,MongoDTO={}, error={}", jsonStr, e.getMessage());
                XxlJobHelper.log("[亚马逊组合库存信息任务] 保存或更新异常,MongoDTO={}, error={}", jsonStr, e.getMessage());
            }
        });

        XxlJobHelper.log("[亚马逊组合库存信息任务] 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 亚马逊请求报表计划任务
     */
    @XxlJob("amazonReportScheduleJob")
    public ReturnT<String> amazonReportScheduleJob() {
        Integer size = 1;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        // 根据报告ID和状态获取reportDocumentId
        XxlJobHelper.log("[亚马逊请求报表计划任务] 任务开始 size={}", size);
        // 根据状态查询未请求的数据
        List<ReportScheduleEntity> reportScheduleEntityList = reportScheduleService.findList(
                ReportScheduleSubscribedStatusEnum.WAIT.getCode(),
                ReportScheduleCancelStatusEnum.NONE.getCode(),
                size
        );
        if (CollectionUtil.isEmpty(reportScheduleEntityList)) {
            XxlJobHelper.log("[亚马逊请求报表计划任务] 任务结束,无需要更新的信息");
            return ReturnT.SUCCESS;
        }
        OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);

        reportScheduleEntityList.forEach(reportSchedule -> {
            try {
                reportHandleService.createReportSchedule(reportSchedule, currentDateTime);
            } catch (Exception e) {
                String errorMsg = JSONUtil.toJsonStr(e);
                XxlJobHelper.log("[亚马逊请求报表计划任务] 创建亚马逊报表计划失败：reportId={}, error={}",
                        reportSchedule.getReportScheduleId(),
                        errorMsg
                );
                throw new ServiceException("亚马逊请求报表计划任务:error=" + errorMsg);
            }
        });
        XxlJobHelper.log("[亚马逊获取报表计划请求任务] 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 创建【亚马逊报告】亚马逊-ERP
     */
    @XxlJob("amazonReportJob")
    public ReturnT<String> amazonReportJob() {
        Integer size = 10;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        // 根据报告ID和状态获取reportDocumentId
        XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 任务开始 size={}", size);
        // TODO 记录请求
        // 根据状态查询未请求的数据
        List<ReportScheduleEntity> reportScheduleEntityList = reportScheduleService.lambdaQuery()
                // 已订阅
                .eq(ReportScheduleEntity::getReportScheduleId, "")
                // 已订阅
                .eq(ReportScheduleEntity::getSubscribedStatus, ReportScheduleSubscribedStatusEnum.ALREADY.getCode())
                // 未取消
                .eq(ReportScheduleEntity::getCancelStatus, ReportScheduleCancelStatusEnum.NONE.getCode())
                // 手动类型
                .eq(ReportScheduleEntity::getSubscribedType, ReportScheduleSubscribedTypeEnum.MANUAL.getCode())
                // 下次创建时间小于等于当前
                .le(ReportScheduleEntity::getFirstNextReportCreationTime, LocalDateTime.now(ZoneId.systemDefault()))
                .orderByAsc(ReportScheduleEntity::getId)
                .last(" LIMIT " + size)
                .list();
        if (CollectionUtil.isEmpty(reportScheduleEntityList)) {
            XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 任务结束,无需要更新的信息");
            return ReturnT.SUCCESS;
        }
        OffsetDateTime currentDateTime = OffsetDateTime.now(ZoneOffset.UTC);

        reportScheduleEntityList.forEach(reportSchedule -> {
            try {
                reportHandleService.createReport(reportSchedule, currentDateTime);
            } catch (Exception e) {
                String errorMsg = JSONUtil.toJsonStr(e);
                XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 创建亚马逊报表计划失败：reportId={}, error={}",
                        reportSchedule.getReportScheduleId(),
                        errorMsg
                );
                throw new ServiceException("创建【亚马逊报告】亚马逊-ERP:error=" + errorMsg);
            }
        });
        XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 任务结束");
        return ReturnT.SUCCESS;
    }

}
