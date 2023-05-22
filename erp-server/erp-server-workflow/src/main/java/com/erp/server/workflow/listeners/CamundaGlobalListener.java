package com.erp.server.workflow.listeners;

import com.erp.server.workflow.service.ProcessManagementService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.spring.boot.starter.event.ExecutionEvent;
import org.camunda.bpm.spring.boot.starter.event.TaskEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 流程监听器
 * @author Cloud
 */
@Component
@Slf4j
public class CamundaGlobalListener {

  @Resource
  private ProcessManagementService processManagementService;

  /**
   * This event is triggered when a task instance is created, assigned, completed, deleted or
   * updated. 2
   * handle mutable task event create
   * @param taskDelegate
   * eventName = create complete
   */
  //@EventListener
  public void onTaskEvent(DelegateTask taskDelegate) {
    // 任务完成时，会触发该事件 eventName = complete create
    log.info("CamundaGlobalListener taskDelegate onTaskEvent = {}", taskDelegate.toString());
    if (TaskListener.EVENTNAME_CREATE.equals(taskDelegate.getEventName())) {
      // 任务创建时的逻辑处理
      log.info("CamundaGlobalListener onTaskEvent Task created: {}", taskDelegate.getName());
      processManagementService.createTaskHandle(taskDelegate);
    }else if (TaskListener.EVENTNAME_COMPLETE.equals(taskDelegate.getEventName())) {
      // 任务完成时的逻辑处理
      log.info("CamundaGlobalListener onTaskEvent Task completed: {}", taskDelegate.getName());
      processManagementService.completeTaskHandle(taskDelegate);
    }


  }

  /**
   * This event is triggered when a task instance is created, assigned, completed, deleted or
   * updated. 1
   * handle immutable task event
   * @param taskEvent
   */
  @EventListener
  public void onTaskEvent(TaskEvent taskEvent) {
    // 任务完成时，会触发该事件 eventType = complete create
//    log.info("Handle immutable task event = {}", taskEvent.toString());

  }

  /**
   * This event is triggered when an execution is created, updated, or deleted. 2
   * handle mutable execution event
   * @param executionDelegate
   * eventName = start， end complete
   */
 // @EventListener
  public void onExecutionEvent(DelegateExecution executionDelegate) {
    log.info("Handle mutable execution event: {}",  executionDelegate.toString());
    if (executionDelegate.getEventName().equals(ExecutionListener.EVENTNAME_START)) {
      // 任务创建时的逻辑处理
      log.info("CamundaGlobalListener onTaskEvent Task created: {}", executionDelegate.getCurrentActivityName());
      processManagementService.startExecutionHandle(executionDelegate);

    }
  }

  /**
   * This event is triggered when an execution is created, updated, or deleted. 1
   * handle immutable execution event
   * @param executionEvent
   */
  @EventListener
  public void onExecutionEvent(ExecutionEvent executionEvent) {
//    log.info("Handle immutable execution event: {}",  executionEvent.toString());

  }

  /**
   * This event is triggered when a history event is produced. 1
   * handle history event
   * @param historyEvent
   */
  @EventListener
  public void onHistoryEvent(HistoryEvent historyEvent) {
    // 任务完成后，会触发该事件 eventType = complete
//    log.info("History event: {}",  JSONUtil.toJsonStr(historyEvent));
  }
 
}