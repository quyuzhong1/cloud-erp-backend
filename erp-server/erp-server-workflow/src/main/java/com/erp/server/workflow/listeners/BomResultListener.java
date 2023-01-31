package com.erp.server.workflow.listeners;

import com.common.core.utils.OkHttpUtils;
import com.erp.common.modules.workflow.dto.ProcessPassDTO;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;
import com.erp.server.workflow.service.WorkflowBusinessProcessService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * bom 审核完成监听
 *
 * @Classname
 * @Description TODO
 * @Date 2023-01-30 18:24
 * @Created by yl
 */
@Service
public class BomResultListener implements ExecutionListener {

    @Resource
    private WorkflowBusinessProcessService workflowBusinessProcessService;

    @Value("${bomProcessPassUrl}")
    private String bomProcessPassUrl;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String processId = delegateExecution.getParentActivityInstanceId();
        ProcessPassDTO processPass = new ProcessPassDTO();
        processPass.setProcessId(processId);
        //业务流程表
        WorkflowBusinessProcessEntity businessProcess = workflowBusinessProcessService.getByProcessId(processId);
        if (businessProcess != null) {
            processPass.setBusinessTableId(businessProcess.getBusinessTableId());
            Map<String, Object> params = new HashMap<>();
            params.put("processId", processId);
            params.put("businessTableId", businessProcess.getBusinessTableId());
            OkHttpUtils.doPostJson(bomProcessPassUrl, params, null);

        }

    }
}
