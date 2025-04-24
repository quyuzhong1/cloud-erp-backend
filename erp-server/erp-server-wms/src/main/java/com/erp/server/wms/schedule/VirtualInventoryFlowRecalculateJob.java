package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.server.wms.service.VirtualTransFlowService;
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
 * 虚拟仓数据重算
 * @author will
 * @date 2024/12/11 19:46
 */
@Slf4j
@Component
public class VirtualInventoryFlowRecalculateJob {

    @Resource
    private VirtualTransFlowService virtualTransFlowService;

    @XxlJob("virtualTransFlowOverride")
    public ReturnT virtualTransFlowOverride() {
        XxlJobHelper.log("虚拟仓库存流水重算定时任务开始执行");
        StopWatch sw = StopWatch.create("virtualTransFlowOverride task");
        sw.start("task start data select");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("虚拟仓库存流水重算定时任务参数：{}", jobParam);
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
        List<String> virtualInventoryIdList = virtualTransFlowService.listVirtualInventoryId(virtualInventoryId, virtualWarehouseId, warehouseId,skuId, fromTable);
        if(CollUtil.isEmpty(virtualInventoryIdList)) {
            log.warn("未找到需要重算的虚拟仓库存流水，库存id:{}, 虚拟仓id:{},实体仓id:{},skuid:{},开始时间:{}", virtualInventoryId, virtualWarehouseId, warehouseId,skuId,startTime);
            return ReturnT.FAIL;
        }
        LocalDate startDate = ObjectUtil.isNull(startTime) ? LocalDate.of(2024,7,6) : startTime.toLocalDate();
        try {
            // 多线程更新库存流水
            CompletableFuture<Void> allOf = CompletableFuture.allOf(virtualInventoryIdList.stream()
                    .map(virtualInvId -> CompletableFuture.runAsync(() ->
                            virtualTransFlowService.overrideVirtualTransFlow(startDate,virtualInvId))
                    ).toArray(CompletableFuture[]::new));
            allOf.thenRun(() -> log.info("虚拟仓库存流水重算，所有任务执行完毕 库存id:{},开始时间:{}", virtualInventoryIdList, startDate)).join();
        }catch (Exception e){
            log.error("virtualInventoryIdList = {},开始时间:{}  重算异常", virtualInventoryIdList,startDate,  e);
            XxlJobHelper.log("virtualInventoryIdList = {},开始时间:{}  重算异常", virtualInventoryIdList, startDate, e);
        }
        sw.stop();
        log.info("virtualInventoryIdList = {},开始时间:{}  重算完成 耗时：{}", virtualInventoryIdList,startDate, sw.prettyPrint(TimeUnit.SECONDS));
        XxlJobHelper.log("virtualInventoryIdList = {},开始时间:{} 重算完成 耗时：{}", virtualInventoryIdList,startDate, sw.prettyPrint(TimeUnit.SECONDS));
        log.info("虚拟仓库存流水重算定时任务执行结束 时间统计为 {}", sw.prettyPrint(TimeUnit.SECONDS));
        XxlJobHelper.log("虚拟仓库存流水重算定时任务执行结束 时间统计为 {}", sw.prettyPrint(TimeUnit.SECONDS));
        return ReturnT.SUCCESS;
    }
}
