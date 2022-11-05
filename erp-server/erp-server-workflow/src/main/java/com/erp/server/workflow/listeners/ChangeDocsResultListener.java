package com.erp.server.workflow.listeners;

import com.common.core.utils.OkHttpUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**   变更文档 审核结果
 * @Classname ChangeDocsResultListener
 * @Description TODO
 * @Date 2022-10-18 11:42
 * @Created by yl
 */
@Service
public class ChangeDocsResultListener implements ExecutionListener {
    @Value("${plmUrl}")
    private String plmUrl;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String  parentActivityInstanceId=  delegateExecution.getParentActivityInstanceId();
        Map<String, Object> params = new HashMap<>();
        params.put("processId", parentActivityInstanceId);
        OkHttpUtils.doPost(plmUrl, params, null);
    }
}
