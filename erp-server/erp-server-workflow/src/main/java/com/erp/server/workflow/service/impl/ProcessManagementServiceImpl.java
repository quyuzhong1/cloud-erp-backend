package com.erp.server.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.CamundaDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.server.workflow.mapper.ProcessManagementMapper;
import com.erp.server.workflow.service.ProcessBusinessService;
import com.erp.server.workflow.service.ProcessDefinitionService;
import com.erp.server.workflow.service.ProcessManagementService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.workflow.service.ProcessTaskManagementService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private TaskService taskService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private AssigneeStrategyService assigneeStrategyService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessManagementDTO.StartResultDTO startProcess(ProcessManagementDTO.StartDTO dto) {
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
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionId, dto.getBusinessId(), dto.getVariablesMap());
        if (Objects.isNull(processInstance)) {
            throw new ServiceException(ApiError.ERROR_94004);
        }
        ExecutionEntity executionEntity = ((ProcessInstanceWithVariablesImpl) processInstance).getExecutionEntity();
        ActivityImpl activity = executionEntity.getActivity();
        List<TaskEntity> tasks = executionEntity.getTasks();

        // 审批任务填充审批信息
        addApproveInfo(tasks, processInstance.getProcessDefinitionId(), dto.getUserId());

        // 保存审批节点数据
        String processInstanceId = processInstance.getProcessInstanceId();
        ProcessManagementEntity insertManagementEntity = new ProcessManagementEntity("", processInstanceId, dto, activity.getActivityId(), processStartTime, processDefinition.getId());
        if (!save(insertManagementEntity)) {
            // 保存流程数据失败
            throw new ServiceException(ApiError.ERROR_94004);
        }
        // 保存流程任务数据
        if(!CollectionUtil.isNotEmpty(tasks)) {
            // 构建processTaskManagementEntity并保存
            List<ProcessTaskManagementEntity> insertTaskList = tasks
                    .stream()
                    .map(task -> new ProcessTaskManagementEntity(processInstanceId,activity.getActivityId(),task.getId(),processStartTime, ApproveStatusEnum.APPROVE_ING))
                    .collect(Collectors.toList());
            processTaskManagementService.saveBatch(insertTaskList);
        }
        ProcessManagementDTO.StartResultDTO result = new ProcessManagementDTO.StartResultDTO(processDefinitionId, processInstanceId, tasks.get(0).getId(),processStartTime, dto.getBusinessId());
        return result;
    }

    /**
     * 审批任务填充审批信息
     *
     * @param tasks
     * @param processDefinitionId
     * @param startUserId 流程发起人
     */
    private void addApproveInfo(List<TaskEntity> tasks, String processDefinitionId, String startUserId) {
        if (CollectionUtil.isEmpty(tasks)) {
            return;
        }
        // 创建modelInstance实例
        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
        tasks.stream().forEach(task -> {
            // 获取审批节点的扩展属性
            UserTask userTask = modelInstance.getModelElementById(task.getTaskDefinitionKey());
            Collection<CamundaProperty> camundaProperties = userTask.getExtensionElements()
                    .getElementsQuery().filterByType(CamundaProperties.class)
                    .singleResult()
                    .getCamundaProperties();
            Map<String, String> propertiesMap = camundaProperties.stream()
                    .collect(Collectors.toMap(CamundaProperty::getCamundaName, CamundaProperty::getCamundaValue));
            CamundaDTO.PropertiesDTO propertiesDTO = BeanUtil.toBean(propertiesMap, CamundaDTO.PropertiesDTO.class);
            // 获取审批人
            // 候选人
            String candidateUsers = propertiesDTO.getCandidateUsers();
            // 使用策略模式获取审批人
            List<String> result = assigneeStrategyService.getResult(propertiesDTO.getAssigneeOption(), propertiesDTO.getAssignee(),startUserId,candidateUsers);
            // 无审批人处理
            if (CollectionUtil.isEmpty(result)) {
                log.warn("任务节点无审批人处理 processDefinitionId = {} ProcessInstanceId = {} taskId={}", processDefinitionId, task.getProcessInstanceId(), task.getId());
                // 无审批人处理
                assigneeEmptyHandler(propertiesDTO.getAssigneeEmpty(), task);
            }




            // TODO 审批条件保存到工作流中
        });
    }

    /**
     * 无审批人处理
     *
     * @param assigneeEmpty 审批为空处理方式
     * @param task         任务
     * @return 审批人
     */
    private List<String> assigneeEmptyHandler(String assigneeEmpty, TaskEntity task) {
        // 审批为空处理方式为空
        if(StrUtil.isEmpty(assigneeEmpty)){
            return Collections.EMPTY_LIST;
        }
        // TODO 审批为空处理方式驳回审批人

        // TODO 审批为空处理方式转上级
        if("".equals(assigneeEmpty)){

        }
        return Collections.EMPTY_LIST;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveProcess(ProcessManagementDTO.ApproveDTO dto) {
        // 查询流程数据
        ProcessManagementEntity processManagement = lambdaQuery()
                .eq(ProcessManagementEntity::getBusinessId, dto.getBusinessId())
                .eq(ProcessManagementEntity::getBusinessKey, dto.getBusinessKey())
                .one();
        if (null == processManagement) {
            throw new ServiceException(ApiError.ERROR_94000);
        }
        // 查询流程任务数据
        ProcessTaskManagementEntity taskManagement = processTaskManagementService.lambdaQuery()
                .eq(ProcessTaskManagementEntity::getProcessInstanceId, processManagement.getProcessInstanceId())
                .eq(ProcessTaskManagementEntity::getCurrentApproverId, dto.getUserId())
                .one();
        // 审核人校验
        if (null == taskManagement) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
        // 审核操作
        // 获取当前任务
        Task currentTask = taskService.createTaskQuery().taskId(taskManagement.getTaskId()).singleResult();
        if(null == currentTask) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
        //添加审批意见
        taskService.createComment(taskManagement.getTaskId(), taskManagement.getProcessInstanceId(), dto.getComment());
        if(ApproveTypeEnum.REJECT.equals(dto.getApproveType())) {
            // 审核不通过
            // 将任务状态设置为失败，并引发reviewFailed异常事件，这将触发流程的异常处理路径
            taskService.handleBpmnError(currentTask.getId(), "reviewFailed");
            // 取消流程实例中所有的当前任务
            runtimeService
                    .createProcessInstanceModification(taskManagement.getProcessInstanceId())
                    .cancelAllForActivity(currentTask.getTaskDefinitionKey())
                    .startBeforeActivity("reviewFailed")
                    .execute();
            // 保存流程任务数据 终止流程
        }else if(ApproveTypeEnum.PASS.equals(dto.getApproveType())) {
            // 审核通过
            taskService.complete(taskManagement.getTaskId(), dto.getVariablesMap());
            // 查询下一个任务
            List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(taskManagement.getProcessInstanceId()).list();
            if(CollectionUtil.isNotEmpty(nextTasks)) {
                // 保存下一个任务
                List<ProcessTaskManagementEntity> insertTaskList = nextTasks
                        .stream()
                        .map(task -> new ProcessTaskManagementEntity(taskManagement.getProcessInstanceId(),task.getTaskDefinitionKey(),task.getId(),LocalDateTime.now(), ApproveStatusEnum.APPROVE_ING))
                        .collect(Collectors.toList());
                processTaskManagementService.saveBatch(insertTaskList);
            }
        }
        // 更新流程任务数据

    }
}
