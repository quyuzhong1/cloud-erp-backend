package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.InventoryClosedRecordEnum;
import com.erp.model.wms.entity.InventoryClosedRecordEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.InventoryFlowOverrideRecordEntity;
import com.erp.model.wms.enums.InventoryFlowOverrideRecordTypeEnum;
import com.erp.server.wms.service.InventoryClosedRecordService;
import com.erp.server.wms.service.InventoryFlowOverrideRecordService;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.TransactionFlowService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 库存流水重算定时任务
 *
 * @Author Cloud
 * @Date 2023/8/2 14:44
 **/
@Slf4j
@Component
public class InventoryFlowRecalculateJob {

    @Resource
    private TransactionFlowService transactionFlowService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private InventoryFlowOverrideRecordService inventoryFlowOverrideRecordService;
    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;

    @XxlJob("inventoryFlowOverride")
    public ReturnT inventoryFlowOverride() {
        XxlJobHelper.log("库存流水重算定时任务开始执行");
        StopWatch sw = StopWatch.create("inventoryFlowOverride task");
        sw.start("task start data select");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("库存流水重算定时任务参数：{}", jobParam);
        LocalDateTime startTime = null;
        String inventoryId;
        String inventoryOrgId = null;
        Boolean fromTable = Boolean.FALSE;
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            startTime = jsonParam.getLocalDateTime("startTime", LocalDateTime.parse("2023-07-06T00:00:00"));
            inventoryId = jsonParam.getStr("inventoryId");
            inventoryOrgId = jsonParam.getStr("inventoryOrgId");
            fromTable = jsonParam.getBool("fromTable");
        } else {
            inventoryId = null;
        }
        if(StrUtil.isNotBlank(inventoryId)){
            InventoryEntity inventory = inventoryService.getById(inventoryId);
            if (null != inventory) {
                inventoryOrgId = inventory.getOrgId();
            } else {
                sw.stop();
                XxlJobHelper.log("inventoryId = {} 对应库存记录不存在", inventoryId);
                return ReturnT.SUCCESS;
            }
        }
        // 1. 查询上次重算结束时间
        Map<String, LocalDateTime> lastTimeOverrideMap = inventoryFlowOverrideRecordService.mapMaxEndTimeGroupByOrgId(inventoryOrgId);

        // 2. 查询关账时间，计算重算时间与关账时间最小值
        Map<String, InventoryClosedRecordEntity> closedDateEntityMap = inventoryClosedRecordService.mapByCategory(InventoryClosedRecordEnum.STK.getCode());
        Map<String, LocalDate> closedDateMap = closedDateEntityMap.values()
                .stream()
                .collect(Collectors.toMap(InventoryClosedRecordEntity::getInventoryOrgId, InventoryClosedRecordEntity::getClosedDate));
        Map<String, String> orgMap = closedDateEntityMap.values()
                .stream()
                .collect(Collectors.toMap(InventoryClosedRecordEntity::getInventoryOrgId, InventoryClosedRecordEntity::getInventoryOrgName));
        // 3. 计算重算时间与关账时间最小值
        Map<String, LocalDate> startTimeMap = getMinTime(lastTimeOverrideMap, closedDateMap, inventoryOrgId);
        sw.stop();
        if(CollUtil.isEmpty(startTimeMap)){
            XxlJobHelper.log("lastTimeOverrideMap = {} 上次重算结束时间， closedDateMap = {}组织关账时间， 都为空", lastTimeOverrideMap, closedDateMap);
            return ReturnT.SUCCESS;
        }
        Set<Map.Entry<String, LocalDate>> entries = startTimeMap.entrySet();
        if(fromTable){
            entries = new HashSet<>();
            entries.add(startTimeMap.entrySet().stream().findFirst().get());
        }
        // 4. 执行重算逻辑
        for (Map.Entry<String, LocalDate> orgStartTimeMap : entries) {
            sw.start("task start orgStartTimeMap = " + orgStartTimeMap);
            String orgName = orgMap.get(orgStartTimeMap.getKey());
            log.info("重算库存流水，组织:{}, 库存id:{}, 开始时间:{}", orgName, inventoryId, startTime);
            // 更新库存流水
            LocalDate startDate = ObjectUtil.isNotEmpty(startTime) ? startTime.toLocalDate() : orgStartTimeMap.getValue();
            // 1. 查询当前组织下所有存在流水的库存id
            List<String> inventoryIdList = transactionFlowService.listByOrgId(startDate, orgStartTimeMap.getKey(), inventoryId, fromTable);
            if(CollUtil.isEmpty(inventoryIdList)) {
                log.warn("未找到需要重算的库存流水，组织:{}, 库存id:{}, 开始时间:{}", orgName, inventoryId, startDate);
                continue;
            }
            try {
                // 多线程更新库存流水
                CompletableFuture<Void> allOf = CompletableFuture.allOf(inventoryIdList.stream()
                        .map(invId -> CompletableFuture.runAsync(() ->
                                transactionFlowService.overrideInventoryFlow(startDate, invId, orgName))
                        ).toArray(CompletableFuture[]::new));
                allOf.thenRun(() -> log.info("###TransactionFlowServiceImpl:::overrideInventoryFlow 库存流水重算，所有任务执行完毕 组织:{}, 库存id:{}, 开始时间:{}", orgName, inventoryId, startDate)).join();
                if(StrUtil.isEmpty(inventoryId) && ObjectUtil.isNotEmpty(inventoryOrgId)){
                    inventoryFlowOverrideRecordService.save(new InventoryFlowOverrideRecordEntity(LocalDateTime.of(startDate, LocalTime.MIN),LocalDateTime.now(), orgStartTimeMap.getKey(),orgName, InventoryFlowOverrideRecordTypeEnum.AUTO));
                }

            }catch (Exception e){
                log.error("inventoryId = {} 组织名称 = {} 重算异常", inventoryId, orgName, e);
                XxlJobHelper.log("inventoryId = {} 组织名称 = {} 重算异常", inventoryId, orgName, e);
            }
            sw.stop();
            log.info("inventoryId = {} 组织名称 = {} 重算完成 耗时：{}", inventoryId, orgName, sw.prettyPrint(TimeUnit.SECONDS));
            XxlJobHelper.log("inventoryId = {} 组织名称 = {} 重算完成 耗时：{}", inventoryId, orgName, sw.prettyPrint(TimeUnit.SECONDS));
        }
        log.info("库存流水重算定时任务执行结束 时间统计为 {}", sw.prettyPrint(TimeUnit.SECONDS));
        XxlJobHelper.log("库存流水重算定时任务执行结束 时间统计为 {}", sw.prettyPrint(TimeUnit.SECONDS));
        return ReturnT.SUCCESS;
    }

    /**
     * 获取关账时间和上次重算结束时间的最小值
     * @param lastTimeOverrideMap  上次重算结束时间
     * @param closedDateMap 关账时间
     * @param inventoryOrgId 库存组织id
     * @return  Map<String, LocalDate>
     */
    private static Map<String, LocalDate> getMinTime(Map<String, LocalDateTime> lastTimeOverrideMap, Map<String, LocalDate> closedDateMap,String inventoryOrgId) {
        Map<String, LocalDate> startTimeMap = closedDateMap.entrySet().stream()
                .collect(Collectors.toMap(
                        // 使用相同的键
                        Map.Entry::getKey,
                        // 比较并取最小值
                        entry -> {
                            LocalDate closedDate = entry.getValue();
                            LocalDateTime lastTimeOverride = lastTimeOverrideMap.get(entry.getKey());
                            // 如果lastTimeOverride存在，转换为LocalDate并取最小值；否则直接返回closedDate
                            if (lastTimeOverride != null) {
                                LocalDate dateFromDateTime = lastTimeOverride.toLocalDate();
                                return dateFromDateTime.isBefore(closedDate) ? dateFromDateTime : closedDate;
                            } else {
                                return closedDate;
                            }
                        }
                ));

        // 处理lastTimeOverrideMap中多余的键
        lastTimeOverrideMap.entrySet().stream()
                .filter(entry -> !closedDateMap.containsKey(entry.getKey()))
                .forEach(entry -> startTimeMap.put(entry.getKey(), entry.getValue().toLocalDate()));
        // 指定组织
        if(StrUtil.isNotBlank(inventoryOrgId)){
            Map<String, LocalDate> result = startTimeMap.entrySet().stream()
                    .filter(entry -> inventoryOrgId.equals(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            // 如果结果为空，设置默认值
            if (result.isEmpty()) {
                result.put(inventoryOrgId, LocalDate.parse("2023-07-06"));
            }
            return result;
        }
        return startTimeMap;
    }
}
