package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.server.wms.service.VirtualTransFlowDetailService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 库存流水重算定时任务
 *
 * @Author Cloud
 * @Date 2023/8/2 14:44
 **/
@Slf4j
@Component
public class VirtualInventoryFlowDetailRecalculateJob {

    @Resource
    private VirtualTransFlowDetailService virtualTransFlowDetailService;

    @XxlJob("virtualTransFlowDetailOverride")
    public ReturnT virtualTransFlowDetailOverride() {
        XxlJobHelper.log("虚拟仓库存流水明细重算定时任务开始执行");
        StopWatch sw = StopWatch.create("virtualInventoryFlowOverride task");
        sw.start("task start data select");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("虚拟仓库存流水明细重算定时任务参数：{}", jobParam);
        String virtualInventoryId = "";
        String virtualWarehouseId = "";
        LocalDateTime startTime = null;
        String warehouseId = "";
        String skuId = "";
        Boolean fromTable = Boolean.FALSE;
        if (CharSequenceUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            virtualInventoryId = jsonParam.getStr("virtualInventoryId");
            virtualWarehouseId = jsonParam.getStr("virtualWarehouseId");
            startTime = jsonParam.getLocalDateTime("startDate", LocalDateTime.parse("2024-07-06T00:00:00"));
            warehouseId = jsonParam.getStr("warehouseId");
            skuId = jsonParam.getStr("skuIdId");
            fromTable = jsonParam.getBool("fromTable");
        }
        // 1. 根据传入参数查询
        List<String> virtualInventoryDetailIdList = virtualTransFlowDetailService.listVirtualInventoryDetailIdList(virtualInventoryId, virtualWarehouseId, warehouseId,skuId, fromTable);
        if(CollUtil.isEmpty(virtualInventoryDetailIdList)) {
            log.warn("未找到需要重算的虚拟仓库存流水明细，库存id:{}, 虚拟仓id:{},实体仓id:{},skuid:{},开始时间:{}", virtualInventoryId, virtualWarehouseId, warehouseId,skuId,startTime);
            return ReturnT.FAIL;
        }
        LocalDate startDate = ObjectUtil.isNull(startTime) ? LocalDate.of(2024,7,6) : startTime.toLocalDate();
        try {
            // 多线程更新库存流水
            CompletableFuture<Void> allOf = CompletableFuture.allOf(virtualInventoryDetailIdList.stream()
                    .map(virtualInvDetailId -> CompletableFuture.runAsync(() ->
                            virtualTransFlowDetailService.overrideVirtualTransFlowDetail(startDate,virtualInvDetailId))
                    ).toArray(CompletableFuture[]::new));
            allOf.thenRun(() -> log.info("虚拟仓库存流水明细重算，所有任务执行完毕 库存id:{},开始时间:{}", virtualInventoryDetailIdList, startDate)).join();
        }catch (Exception e){
            log.error("virtualInventoryDetailIdList = {},开始时间:{}  重算异常", virtualInventoryDetailIdList,startDate,  e);
            XxlJobHelper.log("virtualInventoryDetailIdList = {},开始时间:{}  重算异常", virtualInventoryDetailIdList, startDate, e);
        }
        sw.stop();
        log.info("virtualInventoryDetailIdList = {},开始时间:{}  重算完成 耗时：{}", virtualInventoryDetailIdList,startDate, sw.prettyPrint(TimeUnit.SECONDS));
        XxlJobHelper.log("virtualInventoryDetailIdList = {},开始时间:{} 重算完成 耗时：{}", virtualInventoryDetailIdList,startDate, sw.prettyPrint(TimeUnit.SECONDS));
        log.info("虚拟仓库存流水明细重算定时任务执行结束 时间统计为 {}", sw.prettyPrint(TimeUnit.SECONDS));
        XxlJobHelper.log("虚拟仓库存流水明细重算定时任务执行结束 时间统计为 {}", sw.prettyPrint(TimeUnit.SECONDS));
        return ReturnT.SUCCESS;
    }
}
