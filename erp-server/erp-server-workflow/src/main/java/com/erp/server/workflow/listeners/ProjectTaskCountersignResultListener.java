package com.erp.server.workflow.listeners;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import java.time.LocalDate;

/**
 *  项目任务 审核流程结束 监听
 * @Classname
 * @Description TODO
 * @Date 2022-10-13 15:33
 * @Created by yl
 */
public class ProjectTaskCountersignResultListener  implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
       String  eventName= delegateExecution.getEventName();
       String  parentActivityInstanceId=  delegateExecution.getParentActivityInstanceId();
        System.out.println("eventName==============="+eventName);
       String   instanceId =delegateExecution.getActivityInstanceId();
        System.out.println(LocalDate.now() +"我执行完了啊 ======"+instanceId);
        System.out.println("parentActivityInstanceId================"+parentActivityInstanceId);
    }
}
