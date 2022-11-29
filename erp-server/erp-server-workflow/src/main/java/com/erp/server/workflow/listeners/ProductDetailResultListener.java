package com.erp.server.workflow.listeners;

import com.common.core.utils.OkHttpUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * SKU审核监听
 * @author Will
 * @date: 2022/11/28 12:18
 */
@Service
public class ProductDetailResultListener implements ExecutionListener {
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
