package com.erp.server.oms.schedule;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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

    @XxlJob("workflowTaskRecordRetryJob")
    public ReturnT<String> workflowTaskRecordRetryJob() {
        String jobParam = XxlJobHelper.getJobParam();
        String type = "instance";
        String id = "";
        if (CharSequenceUtil.isNotBlank(jobParam)) {
            JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            type = (String) jsonObject.getOrDefault("type", "instance");
            id = (String) jsonObject.getOrDefault("id", "");
        }
        XxlJobHelper.log("workflowTaskRecordRetryJob 执行开始, type={}, id={}", type, id);
        workflowTaskRecordService.workflowTaskRecordRetryJob(id, type);
        XxlJobHelper.log("workflowTaskRecordRetryJob 执行任务列表结束");
        return ReturnT.SUCCESS;
    }
}
