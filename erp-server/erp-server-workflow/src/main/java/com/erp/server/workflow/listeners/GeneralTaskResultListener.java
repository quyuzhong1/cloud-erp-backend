package com.erp.server.workflow.listeners;

import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 一般任务审核监听
 *
 * @Classname
 * @Description TODO
 * @Date 2022-10-18 11:02
 * @Created by yl
 */
@Service
public class GeneralTaskResultListener implements ExecutionListener {

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        //这个是流程id
        String parentActivityInstanceId = delegateExecution.getParentActivityInstanceId();
        plmTaskFeign.processPass(parentActivityInstanceId);

    }
}
