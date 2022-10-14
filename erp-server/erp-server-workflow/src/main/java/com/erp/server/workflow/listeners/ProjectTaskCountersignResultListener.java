package com.erp.server.workflow.listeners;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

/**
 * @Classname ProjectTaskCountersignResult
 * @Description TODO
 * @Date 2022-10-13 15:33
 * @Created by yl
 */
public class ProjectTaskCountersignResultListener  implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        System.out.println("123");
    }
}
