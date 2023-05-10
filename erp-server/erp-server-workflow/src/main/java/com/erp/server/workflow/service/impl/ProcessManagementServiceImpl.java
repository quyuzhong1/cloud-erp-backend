package com.erp.server.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.workflow.dto.CamundaDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.ProcessStatusEnum;
import com.erp.server.workflow.mapper.ProcessManagementMapper;
import com.erp.server.workflow.service.ProcessBusinessService;
import com.erp.server.workflow.service.ProcessDefinitionService;
import com.erp.server.workflow.service.ProcessManagementService;
import com.erp.server.workflow.service.ProcessTaskManagementService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
@Slf4j
public class ProcessManagementServiceImpl extends SuperServiceImpl<ProcessManagementMapper, ProcessManagementEntity> implements ProcessManagementService {

    @Resource
    private ProcessBusinessService processBusinessService;
    @Resource
    private ProcessDefinitionService processDefinitionService;
    @Resource
    private IdentityService identityService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private ProcessTaskManagementService processTaskManagementService;
    @Resource
    private TaskService taskService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private AssigneeStrategyService assigneeStrategyService;
    @Resource
    private HistoryService historyService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessManagementDTO.StartResultDTO startProcess(ProcessManagementDTO.StartDTO dto) {
        // 判断业务id是否已经存在
        ProcessManagementEntity managementEntity = lambdaQuery()
                .eq(ProcessManagementEntity::getBusinessId, dto.getBusinessId())
                .eq(ProcessManagementEntity::getBusinessKey, dto.getBusinessKey())
                .eq(ProcessManagementEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING)
                .one();
        if (null != managementEntity) {
            // 业务已经发起流程
            throw new ServiceException(ApiError.PROCESS_ALREADY_START);
        }
        // 查询业务数据和关联流程定义
        ProcessBusinessEntity processBusiness = processBusinessService.getProcessBusiness(dto.getBusinessKey());
        if (null == processBusiness) {
           // 业务未绑定流程定义
           throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_BIND);
        }
        String processDefinitionId = processBusiness.getProcessDefinitionId();
        // 查询流程定义
        ProcessDefinitionEntity processDefinition= processDefinitionService.getById(processDefinitionId);
        if (null == processDefinition) {
            // 流程定义不存在
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        // 绑定流程发起人
        identityService.setAuthenticatedUserId(dto.getUserId());
        // 启动流程
        LocalDateTime processStartTime = LocalDateTime.now();
        // 添加流程创建人
        Map<String, Object> variablesMap = dto.getVariablesMap();
        variablesMap.put("creator", dto.getUserId());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionId, dto.getBusinessId(), variablesMap);
        if (Objects.isNull(processInstance)) {
            throw new ServiceException(ApiError.ERROR_94004);
        }
        ExecutionEntity executionEntity = ((ProcessInstanceWithVariablesImpl) processInstance).getExecutionEntity();
        ActivityImpl activity = executionEntity.getActivity();
        List<TaskEntity> tasks = executionEntity.getTasks();
        String processInstanceId = processInstance.getProcessInstanceId();
        // 保存审批节点数据
        ProcessManagementEntity insertManagementEntity = new ProcessManagementEntity(processInstanceId, dto, activity.getActivityId(), processStartTime, processDefinition.getId(),processInstance.getProcessDefinitionId());
        if (!save(insertManagementEntity)) {
            // 保存流程数据失败
            throw new ServiceException(ApiError.ERROR_94004);
        }
        return new ProcessManagementDTO.StartResultDTO(processDefinitionId, processInstanceId, tasks.get(0).getId(),processStartTime, dto.getBusinessId());
    }

    /**
     * 审批任务填充审批信息
     *
     * @param startUserId
     * @param propertiesDTO
     */
    private List<String> addApproveInfo(String startUserId, CamundaDTO.PropertiesDTO propertiesDTO ) {
        // 获取审批人
        String candidateUsers = propertiesDTO.getCandidateUsers();
        // 使用策略模式获取审批人
        List<String> userIds = assigneeStrategyService.getResult(propertiesDTO.getAssigneeOption(), propertiesDTO.getAssignee(),startUserId,candidateUsers);
        // 无审批人处理
        if (CollectionUtil.isEmpty(userIds)) {
            log.warn("任务节点无审批人,开始空审核人处理 startUserId={}", startUserId);
            // 无审批人处理
            userIds = assigneeStrategyService.assigneeEmptyHandler(propertiesDTO.getAssigneeEmpty(), startUserId);
        }
        return userIds;
    }

    /**
     * 获取审批节点的扩展属性
     * @param taskDefinitionKey
     * @param processDefinitionId
     * @return
     */
    private CamundaDTO.PropertiesDTO getProperties(String taskDefinitionKey, String processDefinitionId) {
        // 创建modelInstance实例
        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
        // 获取审批节点的扩展属性
        UserTask userTask = modelInstance.getModelElementById(taskDefinitionKey);
        Collection<CamundaProperty> camundaProperties = userTask.getExtensionElements()
                .getElementsQuery().filterByType(CamundaProperties.class)
                .singleResult()
                .getCamundaProperties();
        Map<String, String> propertiesMap = camundaProperties.stream()
                .collect(Collectors.toMap(CamundaProperty::getCamundaName, CamundaProperty::getCamundaValue));
        CamundaDTO.PropertiesDTO propertiesDTO = BeanUtil.toBean(propertiesMap, CamundaDTO.PropertiesDTO.class);
        return propertiesDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveProcess(ProcessManagementDTO.ApproveDTO dto) {
        // 查询流程数据
        // 查询流程数据
        ProcessManagementDTO.ManagementTaskDTO managementTask  = getTaskByBusiness(dto.getBusinessId(), dto.getBusinessKey(), dto.getUserId());
        // 审核人校验
        if (null == managementTask) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
        // 审核操作
        // 获取当前任务
        Task currentTask = taskService.createTaskQuery().taskId(managementTask.getTaskId()).singleResult();
        if(null == currentTask) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
        //添加审批意见
        String processInstanceId = managementTask.getProcessInstanceId();
        identityService.setAuthenticatedUserId(dto.getUserId());
        if(ApproveTypeEnum.PASS.equals(dto.getApproveType())) {
            // 审核通过
            taskService.createComment(managementTask.getTaskId(), processInstanceId, dto.getComment());
            Map<String, Object> variablesMap = dto.getVariablesMap();
            variablesMap.put("ApproveStatus", "approved");
            taskService.complete(managementTask.getTaskId(), variablesMap);
            updateApprove(managementTask.getTaskManagementId(), dto.getApproveType(), managementTask.getManagementId(), processInstanceId,dto.getComment());
        }

        if(ApproveTypeEnum.REJECT.equals(dto.getApproveType())) {
            // 审核不通过
            runtimeService.createProcessInstanceModification(processInstanceId)
                    //关闭相关任务
                    .cancelAllForActivity(currentTask.getTaskDefinitionKey())
                    .setAnnotation(dto.getComment())
                    .execute();
            // 保存流程任务数据
            updateApprove(managementTask.getTaskManagementId(), dto.getApproveType(), managementTask.getManagementId(), processInstanceId,dto.getComment());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String managementId, String processInstanceId, String comment) {
        // 流程状态 此处传参支持后续其他状态的传入
        ProcessStatusEnum statusEnum =  ApproveTypeEnum.REJECT.equals(approveType) ? ProcessStatusEnum.TERMINATION : ProcessStatusEnum.RUNNING;
        // 根据流程结束时间判定流程是否结束
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        Date endTimeDate = processInstance.getEndTime();
        LocalDateTime endTime = null;
        String nextNodeId = "";
        if(null != endTimeDate){
            // 流程结束
            statusEnum = ProcessStatusEnum.FINISH;
            endTime = LocalDateUtil.date2LocalDateTime(endTimeDate);
        }else {
            // 查询下一个任务
            ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);
            if(null != activityInstance) {
                nextNodeId = activityInstance.getActivityId();
            }
        }

        boolean update = lambdaUpdate()
                .set(ProcessManagementEntity::getProcessStatus, statusEnum)
                .set(ProcessManagementEntity::getCurActivityId, nextNodeId)
                .set(null != endTime, ProcessManagementEntity::getEndTime, endTime)
                .set(null != endTime && ApproveTypeEnum.PASS.equals(approveType) , ProcessManagementEntity::getApproveStatus, ApproveStatusEnum.APPROVE)
                .set(null != endTime && ApproveTypeEnum.REJECT.equals(approveType) , ProcessManagementEntity::getApproveStatus, ApproveStatusEnum.REJECT)
                .eq(ProcessManagementEntity::getId, managementId)
                .update();
        // 更新流程任务数据
        processTaskManagementService.updateApprove(taskId, approveType, comment, "");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startExecutionHandle(DelegateExecution executionDelegate) {
        // 获取当前节点的 CamundaProperty
        FlowElement flowElement = executionDelegate.getBpmnModelElementInstance();
        ExtensionElements extensionElements = flowElement.getExtensionElements();
        if(null == extensionElements){
            executionDelegate.setVariableLocal("userList", Collections.singletonList("admin"));
            return;
        }
        Collection<CamundaProperty> camundaProperties = extensionElements.getElementsQuery().filterByType(CamundaProperties.class).singleResult().getCamundaProperties();
        Map<String, String> propertiesMap = camundaProperties.stream()
                .collect(Collectors.toMap(CamundaProperty::getCamundaName, CamundaProperty::getCamundaValue));
        CamundaDTO.PropertiesDTO propertiesDTO = BeanUtil.toBean(propertiesMap, CamundaDTO.PropertiesDTO.class);
        String startUserId = (String) executionDelegate.getVariable("creator");
        // 获取当前节点的候选人
        List<String> candidateUsers = addApproveInfo(startUserId, propertiesDTO);
        if(CollectionUtil.isEmpty(candidateUsers)){
            log.warn("任务节点无审批人为空 startUserId={}", startUserId);
            // 无审批人终止流程
            runtimeService.deleteProcessInstance(executionDelegate.getProcessInstanceId(), "流程审核人为空, 流程自动关闭");
        }
        // 填充用户变量
        executionDelegate.setVariableLocal("userList", candidateUsers);
        executionDelegate.setVariable("userList", candidateUsers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void back(ProcessManagementDTO.BackDTO dto) {
        // 查询流程数据
        ProcessManagementDTO.ManagementTaskDTO managementTask  = getTaskByBusiness(dto.getBusinessId(), dto.getBusinessKey(), dto.getUserId());
        // 审核人校验
        if (null == managementTask) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
        // 审核操作
        String processInstanceId = managementTask.getProcessInstanceId();
        // 查询历史任务
        List<HistoricActivityInstance> historyActivityList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityId(dto.getActivityId())
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        // 获取驳回节点
        if(CollectionUtil.isEmpty(historyActivityList)){
            throw new ServiceException(ApiError.PROCESS_TASK_NOT_REJECT);
        }
        HistoricActivityInstance historicActivityInstance = historyActivityList.get(0);
        ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);
        if(ApproveTypeEnum.REJECT_APPOINT.equals(dto.getApproveType())) {
            // 驳回指定节点
            runtimeService
                    .createProcessInstanceModification(processInstanceId)
                    .cancelActivityInstance(activityInstance.getId())
                    .setAnnotation(StrUtil.format("驳回到指定节点", managementTask.getBusinessName()))
                    //启动目标活动节点
                    .startBeforeActivity(historicActivityInstance.getActivityId())
                    //流程的可变参数赋值
                    .setVariables(dto.getVariablesMap())
                    .execute();
            // 保存流程任务数据
            backUpdateApprove(managementTask.getTaskManagementId(), managementTask.getManagementId(), dto.getApproveType(),dto.getActivityId(), dto.getComment());
        }
    }

    @Override
    public ProcessManagementDTO.ManagementTaskDTO getTaskByBusiness(String businessId, String businessKey, String userId) {
        return baseMapper.getTaskByBusiness(businessId, businessKey, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean backUpdateApprove(String taskId, String managementId, ApproveTypeEnum approveType, String activityId, String comment) {
        // 更新流程数据
        lambdaUpdate()
                .set(ProcessManagementEntity::getCurActivityId, activityId)
                .eq(ProcessManagementEntity::getId, managementId)
                .update();
        // 更新流程任务数据
        processTaskManagementService.updateApprove(taskId, approveType, comment, activityId);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTaskHandle(DelegateTask task) {
        // 保存流程任务数据 execution 中包含实例信息,
        // 审批任务填充审批信息
        String processDefinitionId = task.getExecution().getProcessDefinitionId();
        String taskDefinitionKey = task.getTaskDefinitionKey();
        CamundaDTO.PropertiesDTO propertiesDTO = getProperties(taskDefinitionKey, processDefinitionId);

        // 保存流程任务数据
        DelegateExecution processInstance = task.getExecution().getProcessInstance();
        String processInstanceId = processInstance.getId();
        String activityName = processInstance.getCurrentActivityName();
        String activityId = processInstance.getCurrentActivityId();
        LocalDateTime processStartTime = LocalDateUtil.date2LocalDateTime(task.getCreateTime());
        String executionId = task.getExecutionId();
        // 查询流程设计审核人处理方式
        ProcessDefinitionEntity processDefinition = processDefinitionService.getById(processDefinitionId.split(":")[0]);
        if(null == processDefinition) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        List<String> candidateUsers = task.getCandidates().stream().map(IdentityLink::getUserId).collect(Collectors.toList());
        candidateUsers.forEach(userId -> {
            // 对去重类型做处理，自动审核通过
            DictBasicEnum reviewSetting = processDefinition.getReviewSetting();
            Optional<ProcessTaskManagementEntity> approveUserId = Optional.empty();
            ProcessTaskManagementEntity insertTask = new ProcessTaskManagementEntity(processInstanceId, activityId, task.getId(), processStartTime, ApproveStatusEnum.APPROVE_ING, propertiesDTO, userId, executionId, activityName);
            processTaskManagementService.saveProcessTask(insertTask);
            // 审核人配置
            if (DictBasicEnum.ADJACENT_DEDUPE.equals(reviewSetting)) {
                // 相邻节点去重
                // 查询当前节点的上一个节点
                LinkedHashMap<String, List<ProcessTaskManagementEntity>> processTaskManagementList = processTaskManagementService.listHisByProcessInstanceId(processInstanceId, MathUtil.ONE);
                approveUserId = processTaskManagementList.entrySet().stream()
                        .findFirst()
                        .get()
                        .getValue().stream().filter(item -> item.getCurApproveId().equals(userId))
                        .findFirst();
            }else if(DictBasicEnum.GLOBAL_DEDUPE.equals(reviewSetting)) {
                // 全局去重
                // 查询已完成审核节点
                LinkedHashMap<String, List<ProcessTaskManagementEntity>> processTaskManagementList = processTaskManagementService.listHisByProcessInstanceId(processInstanceId, null);
                approveUserId = processTaskManagementList.values().stream().flatMap(List::stream)
                        .filter(item -> item.getCurApproveId().equals(userId))
                        .findFirst();
            }
            if (approveUserId.isPresent()) {
                // 审核人已存在，自动审核通过
                taskService.createComment(task.getId(), processInstanceId, "审核人重复,审核自动通过");
                taskService.complete(task.getId());
                updateApprove(approveUserId.get().getId(), ApproveTypeEnum.PASS, insertTask.getId(), processInstanceId, "审核人重复,审核自动通过");
            }
        });
    }

    @Override
    public void completeTaskHandle(DelegateTask taskDelegate) {

    }
}
