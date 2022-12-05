package com.erp.server.workflow.listeners;

import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SKU审核监听
 * @author Will
 * @date: 2022/11/28 12:18
 */
@Service
public class ProductDetailResultListener implements ExecutionListener {

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String  parentActivityInstanceId=  delegateExecution.getParentActivityInstanceId();
        plmTaskFeign.productDetailProcessPass(parentActivityInstanceId);
    }
}
