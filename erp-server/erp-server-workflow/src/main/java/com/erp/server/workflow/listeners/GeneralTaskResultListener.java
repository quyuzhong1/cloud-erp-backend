package com.erp.server.workflow.listeners;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import java.time.LocalDate;

/**
 * 一般任务审核监听
 * @Classname
 * @Description TODO
 * @Date 2022-10-18 11:02
 * @Created by yl
 */
public class GeneralTaskResultListener  implements ExecutionListener {


    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String  parentActivityInstanceId=  delegateExecution.getParentActivityInstanceId();
        System.out.println("parentActivityInstanceId================"+parentActivityInstanceId);
    }
}
