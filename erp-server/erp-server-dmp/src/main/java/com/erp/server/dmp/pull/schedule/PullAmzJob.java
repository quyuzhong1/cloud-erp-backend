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
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
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
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableScheduling
public class PullAmzJob {

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
    private AmzReportHandleService amzReportHandleService;

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

    @Resource
    private CfgAppClientService cfgAppClientService;

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


    /**
     * 根据条件查询mongo亚马逊订单
     * @param downloadStatus
     * @param shopIds
     * @param currentPage
     * @param pageSize
     * @return
     */
    private List<PlatformAmazonOrderDTO> findDownloadStatusAnShopId(Integer downloadStatus, List<String> shopIds, int currentPage, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("downloadStatus").is(downloadStatus)
                .and("shopId").in(shopIds));

        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }

    /**
     * 根据条件查询mongo亚马逊商品
     * @param downloadStatus
     * @param shopIds
     * @param currentPage
     * @param pageSize
     * @return
     */
    private List<PlatformAmazonListingDTO> findProductDownloadStatusAnShopId(Integer downloadStatus, List<String> shopIds, int currentPage, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("downloadStatus").is(downloadStatus)
                .and("shopId").in(shopIds));

        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAmazonListingDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_PRODUCT);
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
        Integer size;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        } else {
            size = 100;
        }
        XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务开始,size={}", size);
        // 根据状态查询未下载数据
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.AMAZON.getCode(), AmazonRequestTypeRateLimiterEnum.ORDER_LIST.getBusinessTypeName());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务结束,未找到需执行的任务");
            return ReturnT.SUCCESS;
        }
        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));

        XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 开始,预计分组线程数量={}", taskGroupMap.size());
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.AMAZON.getCode();
        String business = BusinessTypeEnum.PRODUCT.getCode();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(taskGroupMap.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    // 异步任务的逻辑
                    try {
                        String key = entry.getKey();
                        List<PlatformApiTaskEntity> value = entry.getValue();
                        handlerProductDetail(key, value, size, platform, category, business);
                    } catch (Exception e) {
                        log.info("[拉取亚马逊商品详情任务] amazonProductDetail 执行异常: groupId={}, error={}", entry.getKey(), ExceptionUtil.stacktraceToString(e, 1000));
                        XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 执行异常: groupId={}, error={}", entry.getKey(), ExceptionUtil.stacktraceToString(e, 1000));;
                    }
                    log.info("[拉取亚马逊商品详情任务] amazonProductDetail 当前线程执行完毕");
                    XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 当前线程执行完毕");
                })).toArray(CompletableFuture[]::new));

        allOf.thenRun(() -> XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 所有任务执行完毕")).join();
        return ReturnT.SUCCESS;
    }

    private void handlerProductDetail(String key, List<PlatformApiTaskEntity> value, Integer size, String platform, String category, String business) {
        List<String> shopIds = value.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        // 查询当前分组未下载的mongo订单
        List<PlatformAmazonListingDTO> listingEntityList = this.findProductDownloadStatusAnShopId(0, shopIds, 1, size);
        if (CollectionUtil.isEmpty(listingEntityList)) {
            XxlJobHelper.log("[拉取亚马逊商品详情任务] 无需要执行的详情,shopId={}", JSONUtil.toJsonStr(shopIds));
            return;
        }
        // 查询关联的FNSKU
        List<String> sellerSkuList = listingEntityList.stream()
                .map(PlatformAmazonListingDTO::getSellerSku)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<FbaInventoryEntity> fbaInventoryEntityList = wmsFbaInventoryFeign.findList(sellerSkuList);
        // msku绑定的fnSku是否唯一?
        Map<String, List<FbaInventoryEntity>> fnSkuRelationMap = fbaInventoryEntityList
                .stream()
                .collect(Collectors.groupingBy(FbaInventoryEntity::getMsku));
        // 根据shopId分组
        Map<String, List<PlatformAmazonListingDTO>> dtoGroupList = listingEntityList.stream().collect(Collectors.groupingBy(PlatformAmazonListingDTO::getShopId));


        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.PRODUCT_ITEMS;
        for (Map.Entry<String, List<PlatformAmazonListingDTO>> entry : dtoGroupList.entrySet()) {
            AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(entry.getKey());
            List<PlatformAmazonListingDTO> currentListingDTOList = entry.getValue();
            AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
            // 动态请求配置
            // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
            String redissonKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, platform, key, requestTypeRateLimiterEnum.getBusinessTypeName());
            JSONObject extentJsonObj = requestTypeRateLimiterEnum.getExtentJsonObj();
            extentJsonObj.put(AmazonRequestTypeRateLimiterEnum.limitKey, redissonKey);
            // 根据IdentifiersType分组查询
            List<PlatformAmazonListingDTO> newDtoList = amazonListingHandler.downloadDetailListByIdentifiersType(currentListingDTOList, extentJsonObj, shopInfoDTO, marketPlaceEnum);
            for (PlatformAmazonListingDTO newDto : newDtoList) {
                try {
                    // 关联FNSKU信息
                    FbaInventoryEntity fbaInventoryEntity = fnSkuRelationMap.getOrDefault(newDto.getSellerSku(), Collections.emptyList())
                            .stream()
                            .findFirst()
                            .orElse(null);

                    newDto.setPlatformFnSku(null == fbaInventoryEntity ? "" : fbaInventoryEntity.getFnSku());
                    newDto.setDownloadStatus(1);
                    newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                    newDto.setRedissonKey(null);
                    List<PlatformProductDTO> convertDto = amazonListingHandler.convert(Collections.singletonList(newDto));
                    businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
                    log.info("[拉取亚马逊商品详情任务] amazonProductDetail下载成功，uniqueId={}", newDto.getUniqueId());
                    XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail下载成功，uniqueId={}", newDto.getUniqueId());
                } catch (Exception error) {
                    // 获取锁异常等重试
                    if (error instanceof InterruptedException) {
                        XxlJobHelper.log("请求亚马逊逊获取锁异常：{}", error.getMessage());
                        throw new ServiceException(ApiError.ERROR_1026);
                    }
                    XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail下载失败，uniqueId={}, error={}",
                            newDto.getUniqueId(),
                            ExceptionUtil.stacktraceToString(error, 2000));
                    // 发送预警
                    dmpPushTaskService.sendWarnMsg(newDto.getDmpSyncTaskId());
                }
            }
        }


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

    /**
     * 检查亚马逊店铺/市场任务
     */
    @XxlJob("amazonCheckMarketplace")
    public ReturnT<String> amazonCheckMarketplace() {
        // 报表处理的开始时间
        String jobParamStr = XxlJobHelper.getJobParam();
        Integer size = 10;
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        XxlJobHelper.log("[检查亚马逊店铺/市场任务] 任务开始：size={}", size);

        // 根据状态查询
        ShopInfoDTO.ListParamDTO conditionDTO = new ShopInfoDTO.ListParamDTO();
        conditionDTO.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        conditionDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
//        conditionDTO.setPlatformStatusList(Arrays.asList(ShopPlatformStatusEnum.NONE.getCode(), ShopPlatformStatusEnum.OPEN.getCode()));
        List<ShopInfoEntity> list = shopInfoFeign.listByParams(conditionDTO);

        if (CollectionUtil.isEmpty(list)) {
            XxlJobHelper.log("[检查亚马逊店铺/市场任务] 任务结束：亚马逊已授权店铺列表为空");
            return ReturnT.SUCCESS;
        }
        // 按账号分组
        Map<String, List<ShopInfoEntity>> listMap = list.stream().collect(Collectors.groupingBy(this::getGroupBySellerIdAndMarketPlace));

        for (Map.Entry<String, List<ShopInfoEntity>> entry : listMap.entrySet()) {
            try {
                amzReportHandleService.checkAndUpdateShop(entry.getValue());
                XxlJobHelper.log("[检查亚马逊店铺/市场任务] 检查结束：{}", entry.getKey());
            } catch (Exception e) {
                XxlJobHelper.log("[检查亚马逊店铺/市场任务]检查失败：account={},msg={}, json={}",
                        entry.getKey(),
                        ExceptionUtil.stacktraceToString(e,2000),
                        JSONUtil.toJsonStr(e));
            }
        }

        return ReturnT.SUCCESS;
    }

    /**
     * 通过sellerId和marketplace分组
     */
    private String getGroupBySellerIdAndMarketPlace(ShopInfoEntity entity) {
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(entity.getDictCountryCode());
        if (AmazonMarketplaceEnum.SA.equals(marketplaceEnum) ||
            AmazonMarketplaceEnum.SG.equals(marketplaceEnum) ||
            AmazonMarketplaceEnum.AU.equals(marketplaceEnum) ||
            AmazonMarketplaceEnum.JP.equals(marketplaceEnum)
        ) {
            return StrUtil.format("{}_{}", entity.getPlatformShopCode(), marketplaceEnum.getMarketplaceId());
        } else {
            return StrUtil.format("{}_{}", entity.getPlatformShopCode(), marketplaceEnum.getEndpointsEnum().name());
        }
    }
}
