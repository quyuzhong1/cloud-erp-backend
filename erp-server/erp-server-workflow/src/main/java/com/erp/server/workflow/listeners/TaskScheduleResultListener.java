package com.erp.server.workflow.listeners;

import com.common.core.utils.OkHttpUtils;
import com.erp.model.workflow.dto.ProcessPassDTO;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.workflow.service.WorkflowBusinessProcessService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname TaskScheduleResultListener

 * @Date 2023-02-11 9:42
 * @Created by yl
 */
@Service
public class TaskScheduleResultListener implements ExecutionListener {



    @Resource
    private WorkflowBusinessProcessService workflowBusinessProcessService;


    @Value("${taskScheduleProcessPassUrl}")
    private String taskScheduleProcessPassUrl;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String processId = delegateExecution.getParentActivityInstanceId();
        //业务流程表
        WorkflowBusinessProcessEntity businessProcess = workflowBusinessProcessService.getByProcessId(processId);
        if (businessProcess != null) {
            ProcessPassDTO dto = new ProcessPassDTO();
            dto.setProcessId(processId);
            dto.setBusinessTableId(businessProcess.getBusinessTableId());
            plmTaskFeign.approvalTaskSchedulePass(dto);
        }
    }
}
