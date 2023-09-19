package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.server.wms.service.InventoryHisService;
import com.erp.server.wms.service.TransactionFlowService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存流水重算定时任务
 *
 * @Author Cloud
 * @Date 2023/8/2 14:44
 **/

@Component
public class InventoryFlowRecalculateJob {

    @Resource
    private TransactionFlowService transactionFlowService;
    @Resource
    private InventoryHisService inventoryHisService;

    @XxlJob("inventoryFlowOverride")
    public ReturnT inventoryFlowOverride() {
        XxlJobHelper.log("库存流水重算定时任务开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("库存流水重算定时任务参数：{}", jobParam);
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        String status = null;
        String inventoryId = null;
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            startTime = jsonParam.getLocalDateTime("startTime", LocalDateTime.parse("2023-07-06T00:00:00"));
            endTime = jsonParam.getLocalDateTime("endTime", LocalDateTime.now());
            status = jsonParam.getStr("status");
            inventoryId = jsonParam.getStr("inventoryId");
        }
        transactionFlowService.overrideInventoryFlow(startTime, endTime, status,inventoryId);
        XxlJobHelper.log("库存流水重算定时任务执行结束");
        return null;
    }

    @XxlJob("inventoryHisOverride")
    public ReturnT inventoryHisOverride() {
        XxlJobHelper.log("历史库存重算定时任务开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("历史库存重算定时任务参数：{}", jobParam);
        LocalDate startTime = null;
        LocalDate endTime = null;
        String status = null;
        String inventoryId = null;
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            startTime = LocalDate.parse("2023-07-06");
            endTime = jsonParam.getLocalDateTime("endTime", LocalDateTime.now()).toLocalDate();
            status = jsonParam.getStr("status");
            inventoryId = jsonParam.getStr("inventoryId");
        }
        inventoryHisService.overrideInventoryHis(startTime, endTime, status,inventoryId);
        XxlJobHelper.log("历史库存重算定时任务执行结束");
        return null;
    }


}
