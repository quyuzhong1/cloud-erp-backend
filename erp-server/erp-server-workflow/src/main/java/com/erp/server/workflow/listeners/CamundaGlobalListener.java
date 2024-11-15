package com.erp.server.workflow.listeners;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.erp.server.workflow.service.ProcessManagementService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.runtime.ActivityInstanceState;
import org.camunda.bpm.spring.boot.starter.event.ExecutionEvent;
import org.camunda.bpm.spring.boot.starter.event.TaskEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 流程监听器
 * @author Cloud
 */
@Component
@Slf4j
public class CamundaGlobalListener {

  @Resource
  private ProcessManagementService processManagementService;
  @Resource
  private RuntimeService runtimeService;

  /**
   * This event is triggered when a task instance is created, assigned, completed, deleted or
   * updated. 2
   * handle mutable task event create
   * @param taskDelegate
   * eventName = create complete
   */
  @EventListener
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
    log.debug("Handle immutable task event = {}", taskEvent.toString());
  }

  /**
   * This event is triggered when an execution is created, updated, or deleted. 2
   * handle mutable execution event
   * @param executionDelegate
   * eventName = start， end complete
   */
  @EventListener
  public void onExecutionEvent(DelegateExecution executionDelegate) {
    log.info("Handle mutable execution event: {}",  executionDelegate.toString());
    String key = ((ExecutionEntity) executionDelegate).getProcessDefinition().getKey();
    if(key.startsWith("Process_")){
      log.warn("旧流程不需要走监听器 key = {}", key);
      return;
    }
    int activityInstanceState = ((ExecutionEntity) executionDelegate).getActivityInstanceState();
    String type = (String) ((ExecutionEntity) executionDelegate).getEventSource().getProperties().toMap().get("type");
    List<String> endTypeList = Arrays.asList("endEvent", "noneEndEvent");
    // && ActivityInstanceState.STARTING.getStateCode() == activityInstanceState
    if (ExecutionListener.EVENTNAME_START.equals(executionDelegate.getEventName()) && ActivityInstanceState.STARTING.getStateCode() == activityInstanceState) {
      // 任务创建时的逻辑处理
      log.info("CamundaGlobalListener onTaskEvent Task created: {}", executionDelegate.getCurrentActivityName());

    }else if(ExecutionListener.EVENTNAME_END.equals(executionDelegate.getEventName())) {
      if ((ActivityInstanceState.CANCELED.getStateCode() == activityInstanceState && ObjectUtil.isEmpty(type)) || endTypeList.contains(type)) {
        log.info("CamundaGlobalListener onTaskEvent Task completed: {} {} {} {}", executionDelegate.getEventName(), type, activityInstanceState, executionDelegate.getCurrentActivityName());
        if (ActivityInstanceState.ENDING.getStateCode() == activityInstanceState) {
          log.info("CamundaGlobalListener onTaskEvent Task completed: {}", executionDelegate.getCurrentActivityName());
        }
        // 任务完成时的逻辑处理
        processManagementService.endExecutionHandle(executionDelegate.getProcessInstanceId());
      }

      Object nrOfInstancesObj = executionDelegate.getVariable("nrOfInstances");
      Object loopCounterObj = executionDelegate.getVariable("loopCounter");
      if(ObjectUtil.isEmpty(nrOfInstancesObj) || ObjectUtil.isEmpty(loopCounterObj)){
        executionDelegate.removeVariable("mulUserList");
        executionDelegate.removeVariable("userList");
      }else {
        int nrOfInstances = (int) nrOfInstancesObj;
        int loopCounter = (int) loopCounterObj;
        if (nrOfInstances == loopCounter + 1) {
          executionDelegate.removeVariable("mulUserList");
        }
      }
    }
    // 获取多实例活动的属性和变量
    // 执行take对下个节点赋值变量
    if (executionDelegate.getEventName().equals(ExecutionListener.EVENTNAME_TAKE)) {
      PvmActivity destination = ((ExecutionEntity) executionDelegate).getTransition().getDestination();
      if(null == destination){
        return;
      }
      Map<String, Object> nextActPropertiesMap = destination.getProperties().toMap();
      String nextActType = (String) nextActPropertiesMap.get("type");
      if (CharSequenceUtil.isBlank(nextActType) || !(CharSequenceUtil.equals(nextActType,"userTask") || CharSequenceUtil.equals(nextActType,"multiInstanceBody"))){
        return;
      }
      Boolean isMultiInstance = null != nextActPropertiesMap.get("isMultiInstance") && Boolean.parseBoolean(nextActPropertiesMap.get("isMultiInstance").toString());
      String startUserId = (String) executionDelegate.getVariable("creator");
      List<String> candidateUsers = processManagementService.getCandidateByAct(destination, executionDelegate.getProcessDefinitionId(),startUserId);
      if(Boolean.TRUE.equals(isMultiInstance) || CharSequenceUtil.equals(nextActType, "multiInstanceBody")){
        executionDelegate.setVariable("mulUserList", candidateUsers);
      }else {
        executionDelegate.setVariable("userList", candidateUsers);
      }
    }
  }
  /**
   * This event is triggered when an execution is created, updated, or deleted. 1
   * handle immutable execution event
   * @param executionEvent
   */
  @EventListener
  public void onExecutionEvent(ExecutionEvent executionEvent) {
    log.info("Handle immutable execution event: {}",  executionEvent.toString());
    // 获取流程StartEvent开始事件
    if (executionEvent.getEventName().equals(ExecutionListener.EVENTNAME_START)) {
      log.info("Handle immutable execution event: {}",  executionEvent.toString());
    }
  }

  /**
   * This event is triggered when a history event is produced. 1
   * handle history event
   * @param historyEvent
   */
  @EventListener
  public void onHistoryEvent(HistoryEvent historyEvent) {
//     任务完成后，会触发该事件 eventType = complete
    log.debug("History event: {}",  JSONUtil.toJsonStr(historyEvent));
  }
 
}