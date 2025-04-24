package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.*;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.AmazonJobParamDTO;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonFbaShipmentHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonListingHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonOrderHandler;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
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
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private AmzReportHandleService amzReportHandleService;

    @Resource
    private CfgTimezoneService cfgTimezoneService;

    @Resource
    private AmazonDownloadService amazonDownloadService;

    /**
     * 拉取亚马逊任务
     */
    @XxlJob("amazonExecute")
    public ReturnT<String> execute() {
        // 支持指定分组id执行
        List<String> groupIds = new LinkedList<>();
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            groupIds = JSONUtil.toList(jobParamStr, String.class);
        }
        XxlJobHelper.log("[拉取亚马逊任务] 任务开始：param={} =====", groupIds);
        if (CollectionUtils.isEmpty(groupIds)) {
            // 分组查询
            groupIds = platformApiTaskService.findGroupIdByPlatform(PlatformDictEnum.AMAZON.getCode());
            if (CollectionUtils.isEmpty(groupIds)) {
                XxlJobHelper.log("[拉取亚马逊任务] 任务结束:无任务 =====");
                return ReturnT.SUCCESS;
            }
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
        // 根据状态查询
        ShopInfoDTO.ListParamDTO conditionDTO = new ShopInfoDTO.ListParamDTO();
        conditionDTO.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        conditionDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        List<ShopInfoEntity> list = shopInfoFeign.listByParams(conditionDTO);

        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));
        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 开始,预计分组线程数量={}", taskGroupMap.size());
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.AMAZON.getCode();

        CountDownLatch latch = new CountDownLatch(taskGroupMap.size());

//        taskGroupMap.entrySet().parallelStream().forEach(entry -> {
//                    String key = entry.getKey();
//                    List<PlatformApiTaskEntity> value = entry.getValue();
        taskGroupMap.forEach((key, value) -> threadPoolTaskExecutor.execute(() -> {
            // 处理下载详情
            amazonDownloadService.handlerOrderDetailDownload(key, value, size, platform, category, list);
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
            Thread.currentThread().interrupt();
        }

        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务结束");
        return ReturnT.SUCCESS;
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
        // 根据状态查询
        List<ShopInfoEntity> list = shopInfoFeign.listByParams(new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), null));

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
                    amazonDownloadService.handlerAddressDetail(key, value, size, platform, category, business, list);
                    log.info("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload 当前线程执行完毕");
                    XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload 当前线程执行完毕");
                })).toArray(CompletableFuture[]::new));

        allOf.thenRun(() -> XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonSalesOrderAddressDownload 所有任务执行完毕")).join();

        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderAddressDownload 任务结束");
        return ReturnT.SUCCESS;
    }




    /**
     * 拉取亚马逊商品详情任务
     */
    @XxlJob("amazonProductDetailDownload")
    public ReturnT<String> amazonProductDetail() {
        // 每次请求接口限制数量不能大于20
        // {"shopIdList":[""]}
        String jobParamStr = XxlJobHelper.getJobParam();
        AmazonJobParamDTO.ReportJobDTO jobParamDTO = AmazonJobParamDTO.ReportJobDTO.init(jobParamStr, 20);
        int size = jobParamDTO.getSize() > 20 ? 20 : jobParamDTO.getSize();
        XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务开始,param={}", JSONUtil.toJsonStr(jobParamDTO));
        // 根据状态查询未下载数据
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.AMAZON.getCode(), AmazonRequestTypeRateLimiterEnum.ORDER_LIST.getBusinessTypeName());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务结束,未找到需执行的任务");
            return ReturnT.SUCCESS;
        }
        // 指定店铺
        List<String> finalShopIdList = jobParamDTO.getShopIdList();
        taskList = taskList.stream()
                .filter(e -> CollectionUtils.isEmpty(finalShopIdList) || finalShopIdList.contains(e.getShopId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务结束,未找到指定店铺需执行的任务");
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
                        amazonDownloadService.handlerProductDetail(key, value, size, platform, category, business);
                    } catch (Exception e) {
                        log.info("[拉取亚马逊商品详情任务] amazonProductDetail 执行异常: groupId={}, error={}", entry.getKey(), ExceptionUtil.stacktraceToString(e, 1000));
                        XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 执行异常: groupId={}, error={}", entry.getKey(), ExceptionUtil.stacktraceToString(e, 1000));
                        ;
                    }
                    log.info("[拉取亚马逊商品详情任务] amazonProductDetail 当前线程执行完毕");
                    XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 当前线程执行完毕");
                })).toArray(CompletableFuture[]::new));

        allOf.thenRun(() -> XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 所有任务执行完毕")).join();
        return ReturnT.SUCCESS;
    }




    /**
     * 拉取亚马逊Fba货件详情任务
     */
    @XxlJob("amazonFbaShipmentDetailDownload")
    public ReturnT<String> amazonFbaShipmentDetail() {
        // {"shopIdList":[""]}
        String jobParamStr = XxlJobHelper.getJobParam();
        AmazonJobParamDTO.ReportJobDTO jobParamDTO = AmazonJobParamDTO.ReportJobDTO.init(jobParamStr, 100);
        int size = jobParamDTO.getSize();
        XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 任务开始,param={}", JSONUtil.toJsonStr(jobParamDTO));
        // 根据状态查询未下载数据
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.AMAZON.getCode(), AmazonRequestTypeRateLimiterEnum.FBA_SHIPMENT.getBusinessTypeName());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 任务结束,未找到需执行的任务");
            return ReturnT.SUCCESS;
        }
        // 指定店铺
        List<String> finalShopIdList = jobParamDTO.getShopIdList();
        taskList = taskList.stream()
                .filter(e -> CollectionUtils.isEmpty(finalShopIdList) || finalShopIdList.contains(e.getShopId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 任务结束,未找到指定店铺需执行的任务");
            return ReturnT.SUCCESS;
        }

        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));

        XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 开始,预计分组线程数量={}", taskGroupMap.size());
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.AMAZON.getCode();
        String business = BusinessTypeEnum.FBA_SHIPMENT.getCode();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(taskGroupMap.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    // 异步任务的逻辑
                    try {
                        String key = entry.getKey();
                        List<PlatformApiTaskEntity> value = entry.getValue();
                        amazonDownloadService.handlerFbaShipmentDetail(key, value, size, platform, category, business);
                    } catch (Exception e) {
                        log.info("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 执行异常: groupId={}, error={}", entry.getKey(), ExceptionUtil.stacktraceToString(e, 1000));
                        XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 执行异常: groupId={}, error={}", entry.getKey(), ExceptionUtil.stacktraceToString(e, 1000));
                    }
                    log.info("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 当前线程执行完毕");
                    XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 当前线程执行完毕");
                })).toArray(CompletableFuture[]::new));
        XxlJobHelper.log("[拉取亚马逊Fba货件详情任务] amazonFbaShipmentDetailDownload 任务结束");
        return ReturnT.SUCCESS;
    }




    @XxlJob("amazonCleanExecute")
    public void amazonCleanExecute() {
        String jobParamStr = XxlJobHelper.getJobParam();
        XxlJobHelper.log("[清洗【亚马逊相关待清洗】(mongo->ERP)] amazonCleanExecute 任务开始,param={}", JSONUtil.toJsonStr(jobParamStr));
        List<CleanDataTableEnum> platforms = new LinkedList<>();
        if (StringUtils.isNotBlank(jobParamStr)) {
            CleanDataTableEnum table = CleanDataTableEnum.getByName(jobParamStr);
            if (null != table) {
                platforms = Collections.singletonList(table);
            }
        } else {
            // 所有类型
            platforms = CleanDataTableEnum.getByPlatform(PlatformDictEnum.AMAZON.getCode());
        }
        if (CollectionUtils.isEmpty(platforms)) {
            XxlJobHelper.log("[清洗【亚马逊相关待清洗】(mongo->ERP)] amazonCleanExecute 任务结束,需要清洗的类型");
            return;
        }

        platforms.forEach(cleanDataTableEnum -> {
            JobTaskDTO jobTaskDTO = new JobTaskDTO();
            jobTaskDTO.setPlatformCategory(cleanDataTableEnum.getCategory());
            jobTaskDTO.setDictPlatform(cleanDataTableEnum.getPlatform());
            jobTaskDTO.setBillType(cleanDataTableEnum.getBusiness());
            if (CleanDataTableEnum.AMAZON_ORDER.equals(cleanDataTableEnum)
                    || CleanDataTableEnum.AMAZON_FBA_SHIPMENT.equals(cleanDataTableEnum)
                    || CleanDataTableEnum.AMAZON_PRODUCT.equals(cleanDataTableEnum)
                    || CleanDataTableEnum.AMAZON_SO_OUT_STOCK.equals(cleanDataTableEnum)
            ) {
                // 清洗时检查明细下载状态:DownloadStatus=1
                jobTaskDTO.setClearCheckDownloadStatus(true);
            }
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
                        ExceptionUtil.stacktraceToString(e, 2000),
                        JSONUtil.toJsonStr(e));
            }
        }

        return ReturnT.SUCCESS;
    }


    /**
     * 根据物流销售记录补充订单【亚马逊】->ERP
     */
    @XxlJob("amazonFulfilledCheckOrder")
    public ReturnT<String> amazonFulfilledCheckOrder() {
        Integer size;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        } else {
            size = 100;
        }
        XxlJobHelper.log("[ 根据物流销售记录补充订单【亚马逊】->ERP] amazonFulfilledCheckOrder 任务开始,size={}", size);
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.AMAZON.getCode(), AmazonRequestTypeRateLimiterEnum.ORDER_LIST.getBusinessTypeName());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[ 根据物流销售记录补充订单【亚马逊】->ERP] amazonFulfilledCheckOrder 任务结束,未找到需执行的任务");
            return ReturnT.SUCCESS;
        }
        // 时区配置
        List<CfgTimezoneEntity> timeZoneList = cfgTimezoneService.listAndCache();
        // 店铺信息
        // 查询指定或所有已授权店铺
        Map<String, ShopInfoEntity> shopMap = shopInfoFeign.listByParams(
                        new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.AMAZON.getCode(), null))
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));
        XxlJobHelper.log("[ 根据物流销售记录补充订单【亚马逊】->ERP] amazonFulfilledCheckOrder 开始,预计分组线程数量={}", taskGroupMap.size());
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.AMAZON.getCode();
        String business = BusinessTypeEnum.ORDER.getCode();


        taskGroupMap.forEach((key, value) -> {
            amazonDownloadService.handlerFulfilledCheckOrder(key, value, size, platform, category, business, timeZoneList, shopMap);
            log.info("[ 根据物流销售记录补充订单【亚马逊】->ERP] amazonFulfilledCheckOrder 当前线程执行完毕");
            XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonFulfilledCheckOrder 当前线程执行完毕");
        });

//        CompletableFuture<Void> allOf = CompletableFuture.allOf(taskGroupMap.entrySet().stream()
//                .map(entry -> CompletableFuture.runAsync(() -> {
//                    // 异步任务的逻辑
//                    String key = entry.getKey();
//                    List<PlatformApiTaskEntity> value = entry.getValue();
//                    handlerFulfilledCheckOrder(key, value, size, platform, category, business);
//                    log.info("[ 根据物流销售记录补充订单【亚马逊】->ERP] amazonFulfilledCheckOrder 当前线程执行完毕");
//                    XxlJobHelper.log("[拉取亚马逊订单地址任务] amazonFulfilledCheckOrder 当前线程执行完毕");
//                })).toArray(CompletableFuture[]::new));
//        allOf.thenRun(() -> XxlJobHelper.log("[ 根据物流销售记录补充订单【亚马逊】->ERP] amazonFulfilledCheckOrder 所有任务执行完毕")).join();

        XxlJobHelper.log("[ 根据物流销售记录补充订单【亚马逊】->ERP] amazonFulfilledCheckOrder 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 通过sellerId和marketplace分组
     */
    public String getGroupBySellerIdAndMarketPlace(ShopInfoEntity entity) {
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
