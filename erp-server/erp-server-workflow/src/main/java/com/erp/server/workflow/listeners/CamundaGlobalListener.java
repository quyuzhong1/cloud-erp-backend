package com.erp.server.workflow.listeners;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.model.workflow.entity.ProcessDelegateEntity;
import com.erp.server.workflow.service.ProcessDelegateService;
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
import java.util.*;
import java.util.stream.Collectors;


/**
 * 流程监听器
 * @author Cloud
 */
@Component
@Slf4j
public class CamundaGlobalListener {

  public static final String MUL_USER_LIST = "mulUserList";
  public static final String AUTO_DELEGATE = "auto";

  @Resource
  private ProcessManagementService processManagementService;
  @Resource
  private RuntimeService runtimeService;
  @Resource
  private ProcessDelegateService processDelegateService;

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
    // 处理开始事件
    handleStartEvent(executionDelegate, activityInstanceState);
    // 处理结束事件
    handleEndEvent(executionDelegate, activityInstanceState, type, endTypeList);
    // 处理take事件
    handleTakeEvent(executionDelegate);
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

  /**
   * 处理开始事件
   * @param executionDelegate  执行代理
   * @param activityInstanceState  活动实例状态
   */
  private void handleStartEvent(DelegateExecution executionDelegate, int activityInstanceState) {
    if (ExecutionListener.EVENTNAME_START.equals(executionDelegate.getEventName()) && ActivityInstanceState.STARTING.getStateCode() == activityInstanceState) {
      log.info("CamundaGlobalListener onTaskEvent Task created: {}", executionDelegate.getCurrentActivityName());
    }
  }

  /**
   * 处理结束事件
   * @param executionDelegate 执行代理
   * @param activityInstanceState 活动实例状态
   * @param type 类型
   * @param endTypeList 结束类型列表
   */
  private void handleEndEvent(DelegateExecution executionDelegate, int activityInstanceState, String type, List<String> endTypeList) {
    if (ExecutionListener.EVENTNAME_END.equals(executionDelegate.getEventName())) {
      if ((ActivityInstanceState.CANCELED.getStateCode() == activityInstanceState && ObjectUtil.isEmpty(type)) || endTypeList.contains(type)) {
        log.info("CamundaGlobalListener onTaskEvent Task completed: {} {} {} {}", executionDelegate.getEventName(), type, activityInstanceState, executionDelegate.getCurrentActivityName());
        if (ActivityInstanceState.ENDING.getStateCode() == activityInstanceState) {
          log.info("CamundaGlobalListener onTaskEvent Task completed: {}", executionDelegate.getCurrentActivityName());
        }
        // 流程取消，结束时的逻辑处理
        processManagementService.endExecutionHandle(executionDelegate.getProcessInstanceId());
      }
      clearVariables(executionDelegate);
    }
  }

  /**
   * 清除变量 多实例结束时清除变量
   * @param executionDelegate 执行代理
   */
  private void clearVariables(DelegateExecution executionDelegate) {
    Object nrOfInstancesObj = executionDelegate.getVariable("nrOfInstances");
    Object loopCounterObj = executionDelegate.getVariable("loopCounter");
    if (ObjectUtil.isEmpty(nrOfInstancesObj) || ObjectUtil.isEmpty(loopCounterObj)) {
      // 单实例
      executionDelegate.removeVariable(MUL_USER_LIST);
      executionDelegate.removeVariable("userList");
    } else {
      // 多实例
      int nrOfInstances = (int) nrOfInstancesObj;
      int loopCounter = (int) loopCounterObj;
      if (nrOfInstances == loopCounter + 1) {
        executionDelegate.removeVariable(MUL_USER_LIST);
      }
    }
  }

  /**
   * 处理take事件 执行take对下个节点赋值变量
   * @param executionDelegate 执行代理
   */
  private void handleTakeEvent(DelegateExecution executionDelegate) {
    if (ExecutionListener.EVENTNAME_TAKE.equals(executionDelegate.getEventName())) {
      PvmActivity destination = ((ExecutionEntity) executionDelegate).getTransition().getDestination();
      if (destination == null) {
        return;
      }
      Map<String, Object> nextActPropertiesMap = destination.getProperties().toMap();
      String nextActType = (String) nextActPropertiesMap.get("type");
      if (CharSequenceUtil.isBlank(nextActType) || !(CharSequenceUtil.equals(nextActType, "userTask") || CharSequenceUtil.equals(nextActType, "multiInstanceBody"))) {
        return;
      }
      Boolean isMultiInstance = nextActPropertiesMap.get("isMultiInstance") != null && Boolean.parseBoolean(nextActPropertiesMap.get("isMultiInstance").toString());
      String startUserId = (String) executionDelegate.getVariable("creator");
      List<String> candidateUsers = processManagementService.getCandidateByAct(destination, executionDelegate, startUserId);
      //委托人处理
      List<String> userIdList = getCandidateUsers(executionDelegate,candidateUsers);
      if (Boolean.TRUE.equals(isMultiInstance) || CharSequenceUtil.equals(nextActType, "multiInstanceBody")) {
        executionDelegate.setVariable(MUL_USER_LIST, userIdList);
      } else {
        executionDelegate.setVariable("userList", userIdList);
      }
    }
  }
  /**
   * 委托人处理
   */
  private List<String> getCandidateUsers(DelegateExecution executionDelegate, List<String> candidateUsers) {
    if (ObjectUtil.isEmpty(executionDelegate) || CharSequenceUtil.isBlank(executionDelegate.getProcessDefinitionId())) {
      return candidateUsers;
    }

    // 获取流程定义ID（去除版本号）
    String processDefinitionId = executionDelegate.getProcessDefinitionId().split(":")[0];

    // 查询委托关系
    List<ProcessDelegateEntity> processDelegateList = processDelegateService.getByProcessDefinitionId(processDefinitionId);
    if (CollUtil.isEmpty(processDelegateList)) {
      return candidateUsers;
    }
    //需要委托的人员
      List<ProcessDelegateEntity> needDelegateList = processDelegateList.stream().filter(obj -> candidateUsers.contains(obj.getStartUserId())).collect(Collectors.toList());
    // 替换候选人
   List<String> userIdList = candidateUsers.stream()
            .map(obj -> needDelegateList.stream().filter(e -> CharSequenceUtil.equals(obj,e.getStartUserId())).map(ProcessDelegateEntity::getDelegateUserId).findFirst().orElse(obj))
            .filter(CharSequenceUtil::isNotBlank)
            .distinct()
            .collect(Collectors.toList());

   //存委托标识
    List<Map<String, String>> delegateInfoList = new ArrayList<>();
    for (ProcessDelegateEntity processDelegateEntity : needDelegateList) {
      // 存储委托关系（用于后续任务标记）
      Map<String, String> delegateInfo = new HashMap<>();
      delegateInfo.put("originalUser", processDelegateEntity.getStartUserId());
      delegateInfo.put("delegateUser", processDelegateEntity.getDelegateUserId());
      delegateInfo.put("delegateType", AUTO_DELEGATE); // 委托类型标识
      delegateInfoList.add(delegateInfo);
    }
    // 将委托信息存入流程变量
    LoginUser userInfo = UserContext.getDefaultLoginUser();
    executionDelegate.setVariable("DELEGATE_INFO_" + userInfo.getUid(), delegateInfoList);

    return userIdList;
  }
}