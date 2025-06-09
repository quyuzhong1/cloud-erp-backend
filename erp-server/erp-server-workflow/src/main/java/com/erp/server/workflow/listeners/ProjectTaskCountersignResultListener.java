package com.erp.server.workflow.listeners;


import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 *  项目任务 审核流程结束 监听
 * @Classname

 * @Date 2022-10-13 15:33
 * @Created by yl
 */
@Slf4j
@Service
public class ProjectTaskCountersignResultListener  implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
       String  eventName= delegateExecution.getEventName();
       String  parentActivityInstanceId=  delegateExecution.getParentActivityInstanceId();
        log.warn("eventName==============="+eventName);
       String   instanceId =delegateExecution.getActivityInstanceId();
        log.warn(LocalDate.now() +"我执行完了啊 ======"+instanceId);
        log.warn("parentActivityInstanceId================"+parentActivityInstanceId);
    }
}
