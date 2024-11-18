package com.erp.server.workflow.job;

import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.enums.TimeoutStatusEnum;
import com.erp.server.workflow.service.ProcessManagementService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 超时警告任务
 *
 * @Author Cloud
 * @Date 2023/5/17 12:15
 **/

@Slf4j
@Component
public class TimeoutWarnJob {
    @Resource
    private ProcessManagementService processManagementService;

    /**
     * 超时处理任务
     */
    @XxlJob("TimeoutWarnJob")
    public void timeoutWarnJob() {
        XxlJobHelper.log("TimeoutWarnJob start.");
        // 获取未发送任务
        List<ProcessManagementDTO.ManagementTaskDTO> taskList = processManagementService.listUnsendTask("",TimeoutStatusEnum.UNSEND.getCode());
        if(CollectionUtils.isEmpty(taskList)){
            XxlJobHelper.log("TimeoutWarnJob 需要发送任务列表为空 end.");
            return;
        }

        // 发送消息 通知任务超时
        taskList.forEach(task -> {
            try {
                processManagementService.sendTimeoutWarn(task);
            } catch (Exception e) {
                XxlJobHelper.log("TimeoutWarnJob error.", e);
            }
        });
        XxlJobHelper.log("TimeoutWarnJob end.");
    }

    /**
     * 超时处理任务
     */
    @XxlJob("TimeoutHandleJob")
    public void timeoutHandleJob() {
        XxlJobHelper.log("TimeoutHandleJob start.");
        // 获取未发送任务
        List<ProcessManagementDTO.ManagementTaskDTO> taskList = processManagementService.listUnsendTask("", TimeoutStatusEnum.UNSEND.getCode());
        if(CollectionUtils.isEmpty(taskList)){
            XxlJobHelper.log("TimeoutWarnJob 需要发送任务列表为空 end.");
            return;
        }
        // 发送消息 通知任务超时
        taskList.forEach(task -> {
            try {
                processManagementService.sendTimeoutHandle(task);
            } catch (Exception e) {
                XxlJobHelper.log("TimeoutHandleJob error.", e);
            }
        });
        XxlJobHelper.log("TimeoutHandleJob end.");
    }
}
