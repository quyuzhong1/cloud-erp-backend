package com.erp.server.workflow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;

import com.common.core.enums.ProcessInstanceStateEnum;
import com.common.core.utils.date.DateUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.workflow.dto.*;
import com.erp.server.workflow.mapper.WorkflowMapper;
import com.erp.server.workflow.service.ActHistoryActivityService;
import com.erp.server.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Classname WorkflowServiceImpl
 * @Description TODO
 * @Date 2022-08-16 17:24
 * @Created by yl
 */
@Slf4j
@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ActHistoryActivityService actHistoryActivityService;

    @Resource
    private WorkflowMapper workflowMapper;

    @Autowired
    private IdentityService identityService;

    /**
     * 撤回流程
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-16 18:01
     */
    @Override
    public void withDrawProcess(ProcessBaseDTO dto) {
        String procId = dto.getProcessInstanceId();
        //获取流程状态
        int state = checkProcessInstanceState(procId);
        if (ProcessInstanceStateEnum.PROCESS_ING.getCode() != state) {
            throw new ServiceException(ApiError.ERROR_94000);
        }
        //判断是否有任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(procId).list();
        if (CollectionUtils.isEmpty(taskList)) {
            throw new ServiceException(ApiError.ERROR_94001);
        }

        Task task = taskList.get(0);
        List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .activityType("userTask")
                .finished().orderByHistoricActivityInstanceEndTime()
                .asc().list();
        if (CollectionUtils.isEmpty(historicActivityInstanceList)) {
            throw new ServiceException(ApiError.ERROR_94002);
        }
        ActivityInstance activityInstance = runtimeService.getActivityInstance(task.getProcessInstanceId());
        String toActId = historicActivityInstanceList.get(0).getActivityId();
        String assignee = historicActivityInstanceList.get(0).getAssignee();
        Map<String, Object> taskVariable = new HashMap<>(1);
        //设置当前处理人
        taskVariable.put("assignee", assignee);
        runtimeService.createProcessInstanceModification(procId)
                //关闭相关任务
                .cancelActivityInstance(getInstanceIdForActivity(activityInstance, task.getTaskDefinitionKey()))
                .setAnnotation("进行了撤回到节点操作")
                //启动目标活动节点
                .startBeforeActivity(toActId)
                //流程的可变参数赋值
                .setVariables(taskVariable)
                .execute();
        runtimeService.deleteProcessInstance(task.getProcessInstanceId(), String.format("%s 用户执行了撤回操作", dto.getUserId()));

    }


    /**
     * 取回流程 修改参数
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-17 14:13
     */
    @Override
    public void fetchBackProcess(ApproveProcessDTO dto) {
        String procId = dto.getProcessInstanceId();
        //获取流程状态
        int state = checkProcessInstanceState(procId);
        if (ProcessInstanceStateEnum.PROCESS_ING.getCode() != state) {
            throw new ServiceException(ApiError.ERROR_94000);
        }

        //判断是否有任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(procId).list();
        if (CollectionUtils.isEmpty(taskList)) {
            throw new ServiceException(ApiError.ERROR_94001);
        }

        //获取到流程的节点
        ActivityInstance activityInstance = runtimeService.getActivityInstance(procId);
        if (ObjectUtils.isNull(activityInstance) || ObjectUtils.isEmpty(activityInstance.getChildActivityInstances())) {
            throw new ServiceException(ApiError.ERROR_94002);
        }

        // 判断是否处于第一个用户任务节点
        List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procId)
                .activityType("userTask")
                .unfinished().orderByHistoricActivityInstanceStartTime()
                .asc().list();
        if (CollectionUtils.isEmpty(historicActivityInstanceList) ||
                historicActivityInstanceList.get(0).getActivityId().equals(activityInstance.getChildActivityInstances()[0].getActivityId())) {
            throw new ServiceException(ApiError.ERROR_94003);
        }
        String toActId = historicActivityInstanceList.get(0).getActivityId();
        String assignee = historicActivityInstanceList.get(0).getAssignee();
        //设置当前处理人
        Map<String, Object> taskVariable = new HashMap<>();
        taskVariable.put("assignee", assignee);

        runtimeService.createProcessInstanceModification(procId)
                //关闭相关任务
                .cancelActivityInstance(getInstanceIdForActivity(activityInstance, taskList.get(0).getTaskDefinitionKey()))
                .setAnnotation("进行了取回到节点操作")
                //启动目标活动节点
                .startBeforeActivity(toActId)
                //流程的可变参数赋值
                .setVariables(taskVariable)
                .execute();

        // 删除任务表其它任务
        List<String> taskIdList = new ArrayList<>();
        List<String> actIdList = new ArrayList<>();
        for (int i = 1; i < taskList.size(); i++) {
            taskIdList.add(taskList.get(i).getId());
            actIdList.add(getInstanceIdForActivity(activityInstance, taskList.get(i).getTaskDefinitionKey()));
        }

        // 对于并行的任务，只能取消其中一个，另外的任务取消不了，所以只能自己操作表，去删除、更新数据状态
        if (CollectionUtils.isNotEmpty(taskIdList) && CollectionUtils.isNotEmpty(actIdList)) {
            // 删除ACT_RU_EXECUTION 表中的实例
            workflowMapper.deleteTaskByIdArray(taskIdList);
            // 删除ACT_RU_EXECUTION数据
            workflowMapper.deleteExecutionByProcInstIdAndActInstIdArray(procId, actIdList);
            Date nowDate = new Date();
            // 更新ACT_HI_TASKINST表
            workflowMapper.updateHiTaskInstByIdArray(taskIdList, nowDate);
            // 更新ACT_HI_ACTINST数据
            workflowMapper.updateHiActInstById(actIdList, nowDate);
        }

    }


    /**
     * 驳回到起点
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-17 11:18
     */
    @Override
    public void rejectOriginProcess(ApproveProcessDTO dto) {
        String processInstanceId = dto.getProcessInstanceId();
        Task activeTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();
        HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery()
                .taskId(activeTask.getId())
                .singleResult();

        //获取流程定义
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(taskInstance.getProcessDefinitionId());

        // 获取当前活动
        ActivityImpl currentActivity = processDefinitionEntity.findActivity(taskInstance.getTaskDefinitionKey());
        // 获取起始活动
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .activityType("userTask")
                .processInstanceId(processInstanceId)
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        if (CollectionUtils.isEmpty(historicActivityInstances)) {
            throw new ServiceException(ApiError.ERROR_94001);
        }
        //获取最后一个活动节点
        ActivityImpl lastActivity = processDefinitionEntity.findActivity(historicActivityInstances.get(0).getActivityId());

        // 退回至起点
        runtimeService.createProcessInstanceModification(processInstanceId)
                .cancelAllForActivity(currentActivity.getActivityId())
                .startBeforeActivity(lastActivity.getActivityId())
                .setVariable("denyReason", "驳回到起点")
                .execute();

        //添加审批意见
        taskService.createComment(dto.getTaskId(), dto.getProcessInstanceId(), dto.getComment());
    }


    /**
     * 驳回流程到上一级
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-17 11:47
     */

    @Override
    public void rejectGoBackProcess(ApproveProcessDTO dto) {
        String processInstanceId = dto.getProcessInstanceId();
        //添加意见
        taskService.createComment(dto.getTaskId(), processInstanceId, dto.getComment());
        //获取当前环节实例
        ActivityInstance activity = runtimeService.getActivityInstance(processInstanceId);
        //获取到当前任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();
        String nowActivityId = task.getTaskDefinitionKey();
        String activityId = actHistoryActivityService.getProActivityId(processInstanceId, nowActivityId);
        runtimeService.createProcessInstanceModification(processInstanceId)
                .cancelActivityInstance(activity.getId())
                .setAnnotation("驳回")
                .startBeforeActivity(activityId)
                .execute();
    }


    /**
     * 启动一个流程
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-17 15:32
     */
    @Override
    @Transactional
    public void startProcess(StartProcessDTO dto) {
        try {

            String userId = dto.getUserId();
            //流程发起人
            identityService.setAuthenticatedUserId(userId);
            //查询这个流程 需要审批的人 和对应的参数  是否需要保存 到数据库
            Map<String, Object> parameterMap = dto.getParameterMap();
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(dto.getProcessDefinitionKey(), dto.getBusinessKey(), parameterMap);
            if (Objects.isNull(processInstance)) {
                throw new ServiceException(ApiError.ERROR_94004);
            }
            //流程 id
            String processInstanceId = processInstance.getProcessInstanceId();

            ActivityDTO activityDTO = new ActivityDTO();
            //获取当前环节实例
            ActivityInstance activity = runtimeService.getActivityInstance(processInstanceId);
            activityDTO.setProcessInstanceId(processInstanceId);
            activityDTO.setNowActivityId(activity.getActivityId());

            // 需要保存流程节点信息
            actHistoryActivityService.saveActivity(activityDTO);

        } catch (Exception e) {
            log.error("启动流程出错", e);
            throw new ServiceException(ApiError.ERROR_94004);
        }


    }


    /**
     * 发布流程
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-17 17:52
     */
    @Override
    public void deployDefinitionByResource(DeployProcessDTO dto) {
        Deployment deploy = repositoryService.createDeployment()
                .name(dto.getBusinessName())
                .addClasspathResource(dto.getBpmnName())
                .deploy();
    }


    /**
     * 获取审批记录
     *
     * @param dto
     * @return java.util.List<com.cloud.erp.workflow.modules.workflow.vo.ApproveRecordVO>
     * @author yl
     * @date 2022-08-18 11:34
     */
    @Override
    public List<ApproveRecordShowDTO> queryApproveRecord(ProcessBaseDTO dto) {
        String processInstanceId = dto.getProcessInstanceId();
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
        List<ApproveRecordShowDTO> resultList = new ArrayList<>(list.size());
        for (HistoricActivityInstance item : list) {
            String taskId=item.getTaskId();
            List<Comment> taskComments = taskService.getTaskComments(taskId);
            ApproveRecordShowDTO vo = new ApproveRecordShowDTO();
            vo.setActivityName(item.getActivityName());
            vo.setActivityType(matching(item.getActivityType()));
            vo.setComment(taskComments.size()>0?taskComments.get(0).getFullMessage():"");
            vo.setHandleUserName(StringUtils.isBlank(item.getAssignee())?"无":item.getAssignee());
            vo.setStartTime(DateUtil.conversionDate(item.getStartTime(), DateUtil.fmt));
            vo.setEndTime(DateUtil.conversionDate(item.getEndTime(), DateUtil.fmt));
            vo.setHandleTime(DateUtil.discrepancy(item.getEndTime(),item.getStartTime()));
            resultList.add(vo);
        }

        return resultList;
    }

    public String matching(String activityType){
        String value="";
        switch (activityType){
            case "startEvent":
                value="流程开始";
                break;
            case "userTask":
                value="用户处理";
                break;
            case "noneEndEvent":
                value="流程结束";
                break;
            default:
                value="未知节点";
                break;
        }
        return value;

    }


    protected String getInstanceIdForActivity(ActivityInstance activityInstance, String activityId) {
        ActivityInstance instance = getChildInstanceForActivity(activityInstance, activityId);
        if (instance != null) {
            return instance.getId();
        }
        return null;
    }

    protected ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
        if (activityId.equals(activityInstance.getActivityId())) {
            return activityInstance;
        }
        for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
            ActivityInstance instance = getChildInstanceForActivity(childInstance, activityId);
            if (instance != null) {
                return instance;
            }
        }
        return null;
    }


    /**
     * 获取流程状态
     *
     * @param procId
     * @return int
     * @author yl
     * @date 2022-08-16 18:03
     */
    private int checkProcessInstanceState(String procId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(procId).singleResult();
        //
        if (Objects.isNull(processInstance)) {
            return ProcessInstanceStateEnum.PROCESS_NON_EXISTENT.getCode();
        }
        if (processInstance.isEnded()) {
            return ProcessInstanceStateEnum.PROCESS_ENDED.getCode();
        }
        return ProcessInstanceStateEnum.PROCESS_ING.getCode();
    }
}
