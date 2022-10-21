package com.erp.server.workflow.listeners;

import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  评审任务 会签结束
 * @Description TODO
 * @Date 2022-10-18 11:18
 * @Created by yl
 */
public class ReviewTaskResultListener implements ExecutionListener {

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String  parentActivityInstanceId=  delegateExecution.getParentActivityInstanceId();

        plmTaskFeign.processPass(parentActivityInstanceId);
    }
}
