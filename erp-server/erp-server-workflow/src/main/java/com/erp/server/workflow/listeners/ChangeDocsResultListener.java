package com.erp.server.workflow.listeners;

import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**   变更文档 审核结果
 * @Classname ChangeDocsResultListener
 * @Description TODO
 * @Date 2022-10-18 11:42
 * @Created by yl
 */
@Service
public class ChangeDocsResultListener implements ExecutionListener {
    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String  parentActivityInstanceId=  delegateExecution.getParentActivityInstanceId();
        plmTaskFeign.processPass(parentActivityInstanceId);
    }
}
