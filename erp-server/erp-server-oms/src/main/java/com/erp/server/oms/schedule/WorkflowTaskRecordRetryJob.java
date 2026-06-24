package com.erp.server.oms.schedule;

import com.erp.server.oms.service.WorkflowTaskRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 任务节点记录表补偿重试
 */
@Component
@Slf4j
public class WorkflowTaskRecordRetryJob {

    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;

    @XxlJob("WorkflowTaskRecordRetryJob")
    public ReturnT<String> WorkflowTaskRecordRetryJob() {
        XxlJobHelper.log("WorkflowTaskRecordRetryJob 执行开始");
        workflowTaskRecordService.WorkflowTaskRecordRetryJob("");
        XxlJobHelper.log("WorkflowTaskRecordRetryJob 执行任务列表结束");
        return ReturnT.SUCCESS;
    }
}
