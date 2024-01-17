package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.*;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.entity.ReportScheduleEntity;
import com.erp.model.dmp.enums.ReportScheduleCancelStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedStatusEnum;
import com.erp.model.dmp.enums.ReportScheduleSubscribedTypeEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonFbaShipmentHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonListingHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonOrderHandler;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItemList;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.enums.CleanDataTableEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.erp.server.dmp.service.*;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Resource
    private DmpAmazonFeign dmpAmazonFeign;
    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private CfgSettingService cfgSettingService;

    /**
     * 拉取亚马逊任务
     */
    @XxlJob("amazonExecute")
    public ReturnT<String> execute() {
        XxlJobHelper.log("[拉取亚马逊任务] 任务开始 =====");
        // 分组查询
        List<String> groupIds = platformApiTaskService.findGroupIdByPlatform(PlatformDictEnum.AMAZON.getCode());
        if (CollectionUtils.isEmpty(groupIds)) {
            XxlJobHelper.log("[拉取亚马逊任务] 任务结束:无任务 =====");
            return ReturnT.SUCCESS;
        }
        for (String x : groupIds) {
            try {
                threadPoolTaskExecutor.execute(() -> platformDataThread.executeTask(x, false));
//                platformDataThread.executeTask(x, false);
            } catch (Exception e) {
                XxlJobHelper.log("[拉取亚马逊任务] 执行失败，msg={}", e.getMessage());
            }
        }
        XxlJobHelper.log("[拉取亚马逊任务] 任务结束 =====");
        return ReturnT.SUCCESS;
    }


    /**
     * 拉取亚马逊订单详情任务
     */
    @XxlJob("amazonSalesOrderDetailDownload")
    public ReturnT<String> amazonSalesOrderDetail() {
        Integer size;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        } else {
            size = 100;
        }
        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务开始,size={}", size);
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.AMAZON.getCode(), AmazonRequestTypeRateLimiterEnum.ORDER_LIST.getBusinessTypeName());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务结束,未找到需执行的任务");
            return ReturnT.SUCCESS;
        }
        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));
        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 开始,预计分组线程数量={}", taskGroupMap.size());
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.AMAZON.getCode();
        String business = BusinessTypeEnum.ORDER.getCode();

        CountDownLatch latch = new CountDownLatch(taskGroupMap.size());

//        taskGroupMap.entrySet().parallelStream().forEach(entry -> {
//                    String key = entry.getKey();
//                    List<PlatformApiTaskEntity> value = entry.getValue();
        taskGroupMap.forEach((key, value) -> threadPoolTaskExecutor.execute(() -> {
            // 处理下载详情
            handlerDetailDownload(key, value, size, platform, category, business);
            latch.countDown();
            log.info("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 当前线程执行完毕");
            XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 当前线程执行完毕");
        }));
//        });
        // 等待所有任务执行完毕
        try {
            latch.await();
        } catch (InterruptedException e) {
            XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 监听任务异常:{}", e.getMessage());
        }

        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 处理下载详情
     *
     * @param key
     * @param value
     * @param size
     * @param platform
     * @param category
     * @param business
     */
    private void handlerDetailDownload(String key, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, String business) {
        List<String> shopIds = value.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        // 查询当前分组未下载的mongo订单
        List<PlatformAmazonOrderDTO> orderEntityList = this.findDownloadStatusAnShopId(0, shopIds, 1, size);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            XxlJobHelper.log("[拉取亚马逊订单详情任务] 无需要执行的详情,shopId={}", JSONUtil.toJsonStr(shopIds));
            return;
        }
        for (PlatformAmazonOrderDTO dto : orderEntityList) {
            AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ITEMS;
            try {
                // 动态请求配置
                // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
                String redissonKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, platform, key, requestTypeRateLimiterEnum.getBusinessTypeName());
                JSONObject extentJsonObj = requestTypeRateLimiterEnum.getExtentJsonObj();
                extentJsonObj.put(AmazonRequestTypeRateLimiterEnum.limitKey, redissonKey);
                dto.setRedissonKey(redissonKey);
                PlatformAmazonOrderDTO newDto = amazonOrderHandler.downloadDetail(dto, extentJsonObj);

                newDto.setDownloadStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                newDto.setRedissonKey(null);
                List<PlatformOrderDTO> convertDto = amazonOrderHandler.convert(Collections.singletonList(newDto));
                businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
                log.info("[拉取亚马逊订单详情任务] amazonSalesOrderDetail下载成功，uniqueId={}", dto.getUniqueId());
                XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail下载成功，uniqueId={}", dto.getUniqueId());
            } catch (Exception error) {
                // 获取锁异常等重试
                if (error instanceof InterruptedException) {
                    XxlJobHelper.log("请求亚马逊逊获取锁异常：{}", error.getMessage());
                    throw new ServiceException(ApiError.ERROR_1026);
                }
                XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail下载失败，uniqueId={}, error={}",
                        dto.getUniqueId(),
                        error.getMessage());
                // 发送预警
                dmpPushTaskService.sendWarnMsg(dto.getDmpSyncTaskId());
            }
        }
    }


    /**
     * 拉取亚马逊订单地址任务
     */
    @XxlJob("amazonSalesOrderAddressDownload")
    public ReturnT<String> amazonSalesOrderAddressDownload() {
        Integer size;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        } else {
            size = 100;
        }
        XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload 任务开始,size={}", size);
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.AMAZON.getCode(), AmazonRequestTypeRateLimiterEnum.ORDER_LIST.getBusinessTypeName());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload 任务结束,未找到需执行的任务");
            return ReturnT.SUCCESS;
        }
        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));
        XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload 开始,预计分组线程数量={}", taskGroupMap.size());
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.AMAZON.getCode();
        String business = BusinessTypeEnum.ORDER.getCode();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(taskGroupMap.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    // 异步任务的逻辑
                    String key = entry.getKey();
                    List<PlatformApiTaskEntity> value = entry.getValue();
                    handlerAddressDetail(key, value, size, platform, category, business);
                    log.info("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload 当前线程执行完毕");
                    XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload 当前线程执行完毕");
                })).toArray(CompletableFuture[]::new));

        allOf.thenRun(() -> XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload 所有任务执行完毕")).join();

        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderAddressDownload 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 处理地址详情下载
     *
     * @param key
     * @param value
     * @param size
     * @param platform
     * @param category
     * @param business
     */
    private void handlerAddressDetail(String key, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, String business) {
        // 店铺IDS
        List<String> shopIds = value.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        // 查出下MFN订单
        List<PlatformAmazonOrderDTO> orderEntityList = this.findGroupByFulfillmentChannel(Order.FulfillmentChannelEnum.MFN.getValue(), shopIds, 1, size);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            // 查询AFN订单
            orderEntityList = this.findGroupByFulfillmentChannel(Order.FulfillmentChannelEnum.AFN.getValue(), shopIds, 1, size);
            if (CollectionUtil.isEmpty(orderEntityList)) {
                XxlJobHelper.log("[拉取亚马逊订单地址任务] 无需要查询的地址,shopId={}", JSONUtil.toJsonStr(shopIds));
                return;
            }
        }

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS;
        // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
        String redissonKey = StrUtil.format(RedisCacheConstants.PLATFORM_REQUEST_PREFIX, key);
        for (PlatformAmazonOrderDTO dto : orderEntityList) {
            try {
                // 动态请求配置
                // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
                String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, platform, key, requestTypeRateLimiterEnum.getBusinessTypeName());
                JSONObject extentJsonObj = requestTypeRateLimiterEnum.getExtentJsonObj();
                extentJsonObj.put(AmazonRequestTypeRateLimiterEnum.limitKey, limitKey);
                dto.setRedissonKey(redissonKey);
                // 下载和处理地址
                PlatformAmazonOrderDTO newDto = amazonOrderHandler.downloadAddress(dto, extentJsonObj);
                newDto.setDownloadStatus(1);
                newDto.setDownloadAddressStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                newDto.setRedissonKey(null);
                List<PlatformOrderDTO> convertDto = amazonOrderHandler.convert(Collections.singletonList(newDto));
                // 保存和发送mq
                businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
                log.info("[拉取亚马逊订单地址任务] 无需要查询的地址,shopId={}", JSONUtil.toJsonStr(shopIds));
                XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload下载成功, groupId={}, uniqueId={}", key, dto.getUniqueId());
            } catch (Exception e) {
                // 获取锁异常等重试
                if (e instanceof InterruptedException) {
                    XxlJobHelper.log("请求亚马逊逊获取锁异常：{}", e.getMessage());
                    throw new ServiceException(ApiError.ERROR_1026);
                }
                XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload下载失败，groupId={},uniqueId={}, error={}",
                        key,
                        dto.getUniqueId(),
                        e.getMessage());
                // 发送预警
                dmpPushTaskService.sendWarnMsg(dto.getDmpSyncTaskId());
            }
        }
    }

    private List<PlatformAmazonOrderDTO> findGroupByFulfillmentChannel(String value,
                                                                       List<String> shopIds,
                                                                       int currentPage,
                                                                       Integer pageSize
    ) {
        Query query = new Query();
        query.addCriteria(Criteria.where("order.fulfillmentChannel").is(value)
                .and("shopId").in(shopIds)
                .and("downloadStatus").is(1)
                .and("downloadAddressStatus").is(0));

        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }


    private List<PlatformAmazonOrderDTO> findDownloadStatusAnShopId(Integer downloadStatus, List<String> shopIds, int currentPage, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("downloadStatus").is(downloadStatus)
                .and("shopId").in(shopIds));

        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }


    private Map<String, List<PlatformAmazonOrderDTO>> groupByPlatformShopCode(List<PlatformAmazonOrderDTO> orderEntityList) {
        Map<String, List<PlatformAmazonOrderDTO>> sourceDtoMap = orderEntityList.stream().collect(Collectors.groupingBy(PlatformAmazonOrderDTO::getShopId));
        List<String> shopIds = orderEntityList.stream().map(PlatformAmazonOrderDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIds);
        // 根据平台Seller分组
        Map<String, List<ShopInfoEntity>> shopGroupMap = shopInfoEntityList.stream().collect(Collectors.groupingBy(ShopInfoEntity::getPlatformShopCode));

        Map<String, List<PlatformAmazonOrderDTO>> dtoMap = new HashMap<>();
        shopGroupMap.forEach((key, value) -> {
            List<PlatformAmazonOrderDTO> currentList = new LinkedList<>();
            for (ShopInfoEntity shopInfo : value) {
                currentList.addAll(sourceDtoMap.get(shopInfo.getId()));
            }
            dtoMap.put(key, currentList);
        });
        return dtoMap;
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
        PlatformAmazonListingDTO mongoDTO = PlatformAmazonListingDTO.getByDownloadStatus();
        List<PlatformAmazonListingDTO> list = mongoService.findMongoData(mongoDTO, 1, size, MongoTableNameContant.THIRD_SYSTEM_AMAZON_PRODUCT, PlatformAmazonListingDTO.class);
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
        List<FbaInventoryEntity> fbaInventoryEntityList = wmsFbaInventoryFeign.findList(sellerSkuList);
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
                // 发送预警
                dmpPushTaskService.sendWarnMsg(dto.getDmpSyncTaskId());
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
                // 校验黑名单不调用亚马逊接口
                Boolean skipRequest = this.checkSkipList(dto.getUniqueId());
                // 下载和处理详情
                PlatformAmazonFbaShipmentDTO newDto;
                // 非线上支持手动
                if ((!BusinessCommonConstants.hasProfile("prod") && dto.getUniqueId().contains("手动测试")) || skipRequest) {
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
                // 判断签收时间
                detailListDTO.forEach(e -> {
                    if (e.getReceiveQty() == 0) {
                        e.setReceiveDate(null);
                    }
                });
                shipmentDTO.setDetailList(detailListDTO);
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 推送前：{}", JSONUtil.toJsonStr(newDto));
                businessService.pullDetailProcess(newDto, shipmentDTO, category, platform, business);
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] 推送后：{}", JSONUtil.toJsonStr(newDto));
            } catch (Exception e) {
                XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload下载失败，uniqueId={}, error={}",
                        dto.getUniqueId(),
                        e.getMessage());
                // 发送预警
                dmpPushTaskService.sendWarnMsg(dto.getDmpSyncTaskId());
            }
        });
        XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 检查配置是否存在
     * 存在=跳过请求亚马逊接口
     */
    private Boolean checkSkipList(String uniqueId) {
        String listStr = cfgSettingService.getValue(SettingEnum.AMAZON_FBA_SHIPMENT_SKIP_LIST);
        // 无配置
        if (StringUtils.isBlank(listStr)){
            return false;
        }
        List<String> skipList;
        if (listStr.contains(",")){
            skipList = Arrays.stream(listStr.split(",")).collect(Collectors.toList());;
        } else {
            skipList = Collections.singletonList(listStr);
        }
        return skipList.contains(uniqueId);
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
        Integer size = 20;
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
                XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 当前任务完成,shopId={},mainId={}, reportType={}",
                        reportSchedule.getShopId(),
                        reportSchedule.getId(),
                        reportSchedule.getReportType()
                        );
            } catch (Exception e) {
                XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 创建亚马逊报表计划失败：shopId={}, reportType={}, error={}",
                        reportSchedule.getShopId(),
                        reportSchedule.getReportType(),
                        ExceptionUtil.stacktraceToString(e, 2000)
                );
            }
        });
        XxlJobHelper.log("[创建【亚马逊报告】亚马逊-ERP] 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 拉取亚马逊报表任务
     */
    @XxlJob("amazonReportDownload")
    public ReturnT<String> reportDownload() {
        // 报表处理的开始时间
        String jobParamStr = XxlJobHelper.getJobParam();
        Integer size = 10;
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        XxlJobHelper.log("[拉取亚马逊报表任务] 任务开始：size={}", size);

        // 根据状态查询未下载数据
        ReportInfoMongoDTO orderMongoDTO = ReportInfoMongoDTO.getByNotCheckDownload();
        List<ReportInfoMongoDTO> reportInfoMongoDTOList = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.THIRD_SYSTEM_AMAZON_REPORT, ReportInfoMongoDTO.class);

        if (CollectionUtil.isEmpty(reportInfoMongoDTOList)) {
            XxlJobHelper.log("[拉取亚马逊报表任务] 任务结束：亚马逊已授权店铺列表为空");
            return ReturnT.SUCCESS;
        }

        reportInfoMongoDTOList.forEach(mongoDTO -> {
            try {
                reportHandleService.checkAndDownload(mongoDTO);
            } catch (Exception e) {
                XxlJobHelper.log("[拉取亚马逊报表任务] 拉取亚马逊报表失败：reportId={},msg={}, json={}",
                        mongoDTO.getReportId(),
                        ExceptionUtil.stacktraceToString(e, 2000),
                        JSONUtil.toJsonStr(e)
                );
            }
        });
        XxlJobHelper.log("[拉取亚马逊报表任务] 任务结束");
        return ReturnT.SUCCESS;
    }

    @XxlJob("amazonCleanExecute")
    public void amazonCleanExecute() {
        List<CleanDataTableEnum> platforms = CleanDataTableEnum.getByPlatform(PlatformDictEnum.AMAZON.getCode());
        if (CollectionUtils.isEmpty(platforms)) {
            return;
        }
        platforms.forEach(cleanDataTableEnum -> {
            JobTaskDTO jobTaskDTO = new JobTaskDTO();
            jobTaskDTO.setPlatformCategory(cleanDataTableEnum.getCategory());
            jobTaskDTO.setDictPlatform(cleanDataTableEnum.getPlatform());
            jobTaskDTO.setBillType(cleanDataTableEnum.getBusiness());
            try {
                XxlJobHelper.log("亚马逊开始清洗：{}类{}数据", cleanDataTableEnum.getPlatform(), cleanDataTableEnum.getBusiness());
                platformDataThread.cleanOrder(jobTaskDTO);
                XxlJobHelper.log("亚马逊清洗完成：{}类{}数据", cleanDataTableEnum.getPlatform(), cleanDataTableEnum.getBusiness());
            } catch (Exception e) {
                XxlJobHelper.log("亚马逊清洗异常：{}", e);
            }
        });
    }
}
