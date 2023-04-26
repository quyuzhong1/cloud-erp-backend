package com.erp.server.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
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
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
        // TODO 处理审批条件
        // TODO 审批条件保存到工作流中

        // 保存审批节点数据
        String processInstanceId = processInstance.getProcessInstanceId();
        ProcessManagementEntity insertManagementEntity = new ProcessManagementEntity("", processInstanceId, dto, activity.getActivityId(), processStartTime, processDefinition.getId());
        if (!save(insertManagementEntity)) {
            // 保存流程数据失败
            throw new ServiceException(ApiError.ERROR_94004);
        }
        // 保存流程任务数据
        if(!CollectionUtil.isNotEmpty(tasks)) {
            // 构建processTaskManagementEntity 并保存
            List<ProcessTaskManagementEntity> insertTaskList = tasks
                    .stream()
                    .map(task -> new ProcessTaskManagementEntity(processInstanceId,activity.getActivityId(),task.getId(),processStartTime, ApproveStatusEnum.APPROVE_ING))
                    .collect(Collectors.toList());
            processTaskManagementService.saveBatch(insertTaskList);
        }
        ProcessManagementDTO.StartResultDTO result = new ProcessManagementDTO.StartResultDTO(processDefinitionId, processInstanceId, tasks.get(0).getId(),processStartTime, dto.getBusinessId());
        return result;
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


        }
        // 查询流程任务数据

        // 审核人校验

        // 审核操作
            // 审核通过
            // 审核不通过
        // 下次审核人 保存到流程数据中 更新流程任务数据

    }
}
