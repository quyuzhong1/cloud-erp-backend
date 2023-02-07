package com.erp.server.workflow.listeners;

import com.common.core.utils.OkHttpUtils;
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
 * 产品变更 监听结果
 * @Classname
 * @Description TODO
 * @Date 2023-02-01 15:25
 * @Created by yl
 */
@Service
public class ChangeResultListener implements ExecutionListener {

    @Resource
    private WorkflowBusinessProcessService workflowBusinessProcessService;

    @Value("${changeProcessPassUrl}")
    private String changeProcessPassUrl;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String processId = delegateExecution.getParentActivityInstanceId();
        //业务流程表
        WorkflowBusinessProcessEntity businessProcess = workflowBusinessProcessService.getByProcessId(processId);
        if (businessProcess != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("processId", processId);
            params.put("businessTableId", businessProcess.getBusinessTableId());
            OkHttpUtils.doPostJson(changeProcessPassUrl, params, null);
        }
    }
}
