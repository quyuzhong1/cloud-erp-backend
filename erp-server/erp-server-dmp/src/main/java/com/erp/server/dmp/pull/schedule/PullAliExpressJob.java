package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.dto.AmazonJobParamDTO;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.oms.dto.ShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.server.dmp.enums.CleanDataTableEnum;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.erp.server.dmp.service.AliExpressDownloadService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableScheduling
public class PullAliExpressJob {

    @Resource
    private PlatformDataThread platformDataThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private AliExpressDownloadService aliExpressDownloadService;

    @Resource
    private ShopInfoFeign shopInfoFeign;


    /**
     * 拉取Shopify任务
     */
    @XxlJob("aliExpressExecute")
    public void execute() {
        // 分组查询
        List<String> groupIds = platformApiTaskService.findGroupIdByPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        if (CollectionUtils.isEmpty(groupIds)) {
            XxlJobHelper.log("[拉取速卖通任务] 任务结束:无任务 =====");
            return;
        }
        groupIds.forEach(x -> threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(x, true);
        }));
    }

    @XxlJob("aliExpressCleanExecute")
    public void aliExpressCleanExecute() {
        List<CleanDataTableEnum> platforms = CleanDataTableEnum.getByPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        if (CollectionUtils.isNotEmpty(platforms)) {
            platforms.forEach(cleanDataTableEnum -> {
                JobTaskDTO jobTaskDTO = new JobTaskDTO();
                jobTaskDTO.setPlatformCategory(cleanDataTableEnum.getCategory());
                jobTaskDTO.setDictPlatform(cleanDataTableEnum.getPlatform());
                jobTaskDTO.setBillType(cleanDataTableEnum.getBusiness());
                if (CleanDataTableEnum.ALI_EXPRESS_ORDER.equals(cleanDataTableEnum)
                ) {
                    // 清洗时检查明细下载状态:DownloadStatus=1
                    jobTaskDTO.setClearCheckDownloadStatus(true);
                }
                try {
                    XxlJobHelper.log("开始清洗：{}类{}数据", cleanDataTableEnum.getPlatform(), cleanDataTableEnum.getBusiness());
                    platformDataThread.cleanOrder(jobTaskDTO);
                    XxlJobHelper.log("清洗完成：{}类{}数据", cleanDataTableEnum.getPlatform(), cleanDataTableEnum.getBusiness());
                } catch (Exception e) {
                    XxlJobHelper.log("清洗异常：{}", e);
                }
            });
        }
    }

    /**
     * 拉取速卖通地址 解密
     */
    @XxlJob("aliExpressAddressExecute")
    public void aliExpressAddressExecute() {
        Integer size;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        } else {
            size = 100;
        }
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.ALI_EXPRESS.getCode(), CleanDataTableEnum.ALI_EXPRESS_ORDER.getBusiness());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取速卖通订单地址任务] aliExpressAddressExecute 任务结束,未找到需执行的任务");
        }
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.ALI_EXPRESS.getCode();
        String business = BusinessTypeEnum.ORDER.getCode();
        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));
        for (Map.Entry<String, List<PlatformApiTaskEntity>> entry : taskGroupMap.entrySet()) {
            String key = entry.getKey();
            List<PlatformApiTaskEntity> list = entry.getValue();
            aliExpressDownloadService.handlerAddressDetail(key, list, platform, category, business, size);
        }
    }

    /**
     * 下载【订单详情】(速卖通-ERP)
     */
    @XxlJob("aliExpressOrderDetailDownload")
    public ReturnT<String> aliExpressOrderDetailDownload() {
        Integer size;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        } else {
            size = 100;
        }
        XxlJobHelper.log("[拉取速卖通订单详情任务] aliExpressOrderDetailDownload 任务开始,size={}", size);
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.ALI_EXPRESS.getCode(), "order");
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取速卖通订单详情任务] aliExpressOrderDetailDownload 任务结束,未找到需执行的任务");
            return ReturnT.SUCCESS;
        }
        // 根据状态查询
        ShopInfoDTO.ListParamDTO conditionDTO = new ShopInfoDTO.ListParamDTO();
        conditionDTO.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        conditionDTO.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        List<ShopInfoEntity> list = shopInfoFeign.listByParams(conditionDTO);

        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));
        XxlJobHelper.log("[拉取速卖通订单详情任务] aliExpressOrderDetailDownload 开始,预计分组线程数量={}", taskGroupMap.size());
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.ALI_EXPRESS.getCode();

        CountDownLatch latch = new CountDownLatch(taskGroupMap.size());

        taskGroupMap.forEach((key, value) -> threadPoolTaskExecutor.execute(() -> {
            // 处理下载详情
            aliExpressDownloadService.handlerOrderDetailDownload(value, size, platform, category, list);
            latch.countDown();
            log.info("[拉取速卖通订单详情任务] aliExpressOrderDetailDownload 当前线程执行完毕");
            XxlJobHelper.log("[拉取速卖通订单详情任务] aliExpressOrderDetailDownload 当前线程执行完毕");
        }));
        // 等待所有任务执行完毕
        try {
            latch.await();
        } catch (InterruptedException e) {
            XxlJobHelper.log("[拉取速卖通订单详情任务] aliExpressOrderDetailDownload 监听任务异常:{}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        XxlJobHelper.log("[拉取速卖通订单详情任务] aliExpressOrderDetailDownload 任务结束");
        return ReturnT.SUCCESS;
    }


    /**
     * 拉取速卖通发货单任务
     */
    @XxlJob("aliExpressSoDeliveryDownload")
    public ReturnT<String> aliExpressSoDeliveryDownload() {
        Integer size;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        } else {
            size = 100;
        }
        XxlJobHelper.log("[拉取速卖通发货单任务] aliExpressSoDeliveryDownload 任务开始,size={}", size);
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.ALI_EXPRESS.getCode(), "order");
        if (org.springframework.util.CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取速卖通发货单任务] aliExpressSoDeliveryDownload 任务结束,未找到需执行的任务");
            return ReturnT.SUCCESS;
        }
        // 根据状态查询
        List<ShopInfoEntity> list = shopInfoFeign.listByParams(new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.ALI_EXPRESS.getCode(), null));

        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));
        XxlJobHelper.log("[拉取速卖通发货单任务] aliExpressSoDeliveryDownload 开始,预计分组线程数量={}", taskGroupMap.size());
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.ALI_EXPRESS.getCode();

        CountDownLatch latch = new CountDownLatch(taskGroupMap.size());

        taskGroupMap.forEach((key, value) -> threadPoolTaskExecutor.execute(() -> {
            // 处理下载详情
            aliExpressDownloadService.handlerSoDeliveryDownload(value, size, platform, category, list);
            latch.countDown();
            log.info("[拉取速卖通发货单任务] aliExpressSoDeliveryDownload 当前线程执行完毕");
            XxlJobHelper.log("[拉取速卖通发货单任务] aliExpressSoDeliveryDownload 当前线程执行完毕");
        }));
        // 等待所有任务执行完毕
        try {
            latch.await();
        } catch (InterruptedException e) {
            XxlJobHelper.log("[拉取速卖通发货单任务] aliExpressSoDeliveryDownload 监听任务异常:{}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        XxlJobHelper.log("[拉取速卖通发货单任务] aliExpressSoDeliveryDownload 任务结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 拉取速卖通发货单明细任务
     */
    @XxlJob("aliExpressSoDeliveryDetailDownload")
    public ReturnT<String> aliExpressSoDeliveryDetailDownload() {
        Integer size;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        } else {
            size = 100;
        }
        XxlJobHelper.log("[拉取速卖通发货单明细任务] aliExpressSoDeliveryDetailDownload 任务开始,size={}", size);
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.ALI_EXPRESS.getCode(), "order");
        if (org.springframework.util.CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取速卖通发货单明细任务] aliExpressSoDeliveryDetailDownload 任务结束,未找到需执行的任务");
            return ReturnT.SUCCESS;
        }
        // 根据状态查询
        List<ShopInfoEntity> list = shopInfoFeign.listByParams(new ShopInfoDTO.ListParamDTO(AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.ALI_EXPRESS.getCode(), null));

        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));
        XxlJobHelper.log("[拉取速卖通发货单明细任务] aliExpressSoDeliveryDetailDownload 开始,预计分组线程数量={}", taskGroupMap.size());
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.ALI_EXPRESS.getCode();

        CountDownLatch latch = new CountDownLatch(taskGroupMap.size());

        taskGroupMap.forEach((key, value) -> threadPoolTaskExecutor.execute(() -> {
            // 处理下载详情
            aliExpressDownloadService.handlerSoDeliveryDetailDownload(value, size, platform, category, list);
            latch.countDown();
            log.info("[拉取速卖通发货单明细任务] aliExpressSoDeliveryDetailDownload 当前线程执行完毕");
            XxlJobHelper.log("[拉取速卖通发货单明细任务] aliExpressSoDeliveryDetailDownload 当前线程执行完毕");
        }));
        // 等待所有任务执行完毕
        try {
            latch.await();
        } catch (InterruptedException e) {
            XxlJobHelper.log("[拉取速卖通发货单明细任务] aliExpressSoDeliveryDetailDownload 监听任务异常:{}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        XxlJobHelper.log("[拉取速卖通发货单明细任务] aliExpressSoDeliveryDetailDownload 任务结束");
        return ReturnT.SUCCESS;
    }
}
