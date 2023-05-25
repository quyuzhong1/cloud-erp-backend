package com.erp.server.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.model.workflow.dto.CamundaDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.ProcessStatusEnum;
import com.erp.model.workflow.enums.TimeoutStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.mapper.ProcessManagementMapper;
import com.erp.server.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
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
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ProcessTaskCcService processTaskCcService;
    @Resource
    private MQProducerService mqProducerService;


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
        ProcessManagementEntity insertManagementEntity = new ProcessManagementEntity(processInstanceId, dto, activity.getActivityId(), processStartTime, processDefinition,processInstance.getProcessDefinitionId());
        if (!save(insertManagementEntity)) {
            // 保存流程数据失败
            throw new ServiceException(ApiError.ERROR_94004);
        }
        return new ProcessManagementDTO.StartResultDTO(processDefinitionId, processInstanceId, tasks.get(0).getId(),processStartTime, dto.getBusinessId(), dto.getBusinessName());
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
        ExtensionElements extensionElements = userTask.getExtensionElements();
        if (null == extensionElements) {
            return null;
        }
        Collection<CamundaProperty> camundaProperties = extensionElements
                .getElementsQuery().filterByType(CamundaProperties.class)
                .singleResult()
                .getCamundaProperties();
        if (CollectionUtil.isEmpty(camundaProperties)) {
            return null;
        }
        Map<String, String> propertiesMap = camundaProperties.stream()
                .collect(Collectors.toMap(CamundaProperty::getCamundaName, CamundaProperty::getCamundaValue));
        return BeanUtil.toBean(propertiesMap, CamundaDTO.PropertiesDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessManagementDTO.ApproveResultDTO approveProcess(ProcessManagementDTO.ApproveDTO dto) {
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
            updateApprove(managementTask.getTaskManagementId(), dto.getApproveType(), managementTask.getManagementId(), processInstanceId,dto.getComment());
            Map<String, Object> variablesMap = dto.getVariablesMap();
            variablesMap.put("ApproveStatus", "approved");
            taskService.complete(managementTask.getTaskId(), variablesMap);
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
        return new ProcessManagementDTO.ApproveResultDTO(currentTask.getProcessDefinitionId(), currentTask.getProcessInstanceId(), managementTask.getBusinessId(), managementTask.getBusinessName(),currentTask.getId(),currentTask.getName(), currentTask.getTaskDefinitionKey());
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
        ProcessManagementEntity managementEntity = getById(managementId);
        processTaskManagementService.updateApprove(taskId, approveType, comment, "", managementEntity);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startExecutionHandle(DelegateExecution executionDelegate) {
        // 获取当前节点的 CamundaProperty
        FlowElement flowElement = executionDelegate.getBpmnModelElementInstance();
        ExtensionElements extensionElements = flowElement.getExtensionElements();
        if(null == extensionElements){
            log.warn("流程设计未配置扩展属性, processDefinitionId: {}, taskDefinitionKey: {}", executionDelegate.getProcessDefinitionId(), executionDelegate.getProcessInstanceId());
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessManagementDTO.BackResultDTO back(ProcessManagementDTO.BackDTO dto) {
        // 查询流程数据
        ProcessManagementDTO.ManagementTaskDTO managementTask  = getTaskByBusiness(dto.getBusinessId(), dto.getBusinessKey(), dto.getUserId());
        // 审核人校验
        if (null == managementTask) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
        // 审核操作
        String processInstanceId = managementTask.getProcessInstanceId();

        // 获取当前任务
        Task currentTask = taskService.createTaskQuery().taskId(managementTask.getTaskId()).singleResult();
        if(null == currentTask) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
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

        return new ProcessManagementDTO.BackResultDTO(activityInstance.getProcessDefinitionId(), activityInstance.getProcessInstanceId(), managementTask.getBusinessId(), managementTask.getBusinessName(), historicActivityInstance.getActivityId(), historicActivityInstance.getActivityName());
    }

    @Override
    public ProcessManagementDTO.ManagementTaskDTO getTaskByBusiness(String businessId, String businessKey, String userId) {
        return baseMapper.getTaskByBusiness(businessId, businessKey, null);
    }

    private List<ProcessManagementDTO.ManagementTaskDTO> listTaskByBusiness(String businessId, String businessKey) {
        return baseMapper.listTaskByBusiness(businessId, businessKey);
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
        ProcessManagementEntity managementEntity = getById(managementId);
        processTaskManagementService.updateApprove(taskId, approveType, comment, activityId, managementEntity);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean transfer(ProcessManagementDTO.TransferDTO dto) {
        // 查询当前执行任务
        ProcessManagementDTO.ManagementTaskDTO managementTask  = getTaskByBusiness(dto.getBusinessId(), dto.getBusinessKey(), dto.getSourceUserId());
        if (null == managementTask) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
        String taskId = managementTask.getTaskId();
        // 查询当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(null == task){
            throw new ServiceException(ApiError.ERROR_TASK_AUDIT_STATUS);
        }
        // 转发任务给目标人员
        identityService.setAuthenticatedUserId(dto.getSourceUserId());
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getTargetUserId());
        if(null == findUserDTO){
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        taskService.delegateTask(task.getId(), dto.getTargetUserId());
        // 更新流程任务数据
        processTaskManagementService.updateTransfer(taskId, dto.getTargetUserId(),findUserDTO.getUserName(), dto.getSourceUserId(), dto.getRemark());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessManagementDTO.RevokeResultDTO revoke(ProcessManagementDTO.RevokeDTO dto) {
        // 查询流程实例
        ProcessManagementDTO.ManagementTaskDTO managementTask  = getTaskByBusiness(dto.getBusinessId(), dto.getBusinessKey(), dto.getUserId());
        if (null == managementTask) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
        // 查询当前实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(managementTask.getProcessInstanceId()).singleResult();
        if(null == processInstance || processInstance.isEnded()){
            throw new ServiceException(ApiError.ERROR_94000);
        }
        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processInstance.getProcessDefinitionId());
        if (null == modelInstance) {
            throw new ServiceException(ApiError.ERROR_94001);
        }
        String initialActivityId = null;
        Collection<FlowElement> flowElements = modelInstance.getModelElementsByType(FlowElement.class);
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof StartEvent) {
                initialActivityId = flowElement.getId();
                break;
            }
        }
        // 查询所有执行中的节点
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        for (Execution execution : executions) {
            if (execution instanceof ExecutionEntity) {
                ExecutionEntity executionEntity = (ExecutionEntity) execution;
                // 根据节点状态进行撤回操作
                if (executionEntity.isActive() && !executionEntity.isEnded() && !executionEntity.getActivityId().equals(initialActivityId)) {
                    runtimeService.deleteProcessInstance(execution.getProcessInstanceId(), "process revoke", true);
                    historyService.deleteHistoricProcessInstance(execution.getProcessInstanceId());
                    // 在这里可以记录流程撤回日志
                    break;
                }
            }
        }
        // 更新流程任务数据
        removeByProcessInstanceId(processInstance.getProcessInstanceId());
        return new ProcessManagementDTO.RevokeResultDTO(processInstance.getProcessDefinitionId(), processInstance.getProcessInstanceId(), managementTask.getBusinessId(), managementTask.getBusinessName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeByProcessInstanceId(String processInstanceId) {
        remove(new LambdaQueryWrapper<ProcessManagementEntity>().eq(ProcessManagementEntity::getProcessInstanceId, processInstanceId));
        processTaskManagementService.removeByProcessInstanceId(processInstanceId);
        return Boolean.TRUE;
    }

    @Override
    public List<ProcessManagementDTO.HistoryActivityResultDTO> historyActivity(ProcessManagementDTO.HistoryActivityDTO dto) {
        List<ProcessManagementDTO.ManagementTaskDTO> taskList  = listTaskByBusiness(dto.getBusinessId(), dto.getBusinessKey());
        if(CollectionUtil.isEmpty(taskList)){
            return Collections.emptyList();
        }
        return taskList.stream()
                .map(ProcessManagementDTO.HistoryActivityResultDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public PagingVO<ProcessManagementDTO.PagingResultDTO> paging(PagingDTO<ProcessManagementDTO.SearchDTO> pageDTO) {
        // 查询流程实例
        Page<ProcessManagementDTO.PagingResultDTO> query = new Page<>(pageDTO.getCurrPage(), pageDTO.getPageSize());
        IPage<ProcessManagementDTO.PagingResultDTO> pageData = baseMapper.paging(query, pageDTO.getParams());
        pageData.getRecords().stream().peek(record ->{
            if(null != record.getProcessStatus()){
                record.setProcessStatusName(record.getProcessStatus().getName());
            }
            if(null != record.getTaskStatus()){
                record.setTaskStatusName(record.getTaskStatus().getName());
            }
        }).collect(Collectors.toList());
        return new PagingVO<>(pageData);
    }

    @Override
    public void export(ProcessManagementDTO.ExportDTO dto, HttpServletResponse response) throws Exception {
        // 查询流程实例
        List<ProcessManagementDTO.PagingResultDTO> list = baseMapper.export(dto);
        if (CollectionUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        list.stream().peek(x -> x.setProcessStatusName(x.getProcessStatus().getName())).collect(Collectors.toList());
        List<ProcessManagementDTO.ExportResultDTO> exportList = BeanUtil.copyToList(list, ProcessManagementDTO.ExportResultDTO.class);
        // 导出
        String excelPath = "excel/process_management.xlsx";
        String name = "流程管理";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        new ExcelPrintUtils().patchExport(exportList, response, StrUtil.format("{}_{}", name, date), excelPath);
    }

    @Override
    public ProcessManagementDTO.ProcessResultDTO progress(ProcessManagementDTO.ProgressDTO dto) {
        // Get the process definition ID from the process instance ID
        String processDefinitionId = runtimeService.createProcessInstanceQuery()
                .processInstanceId(dto.getProcessInstanceId())
                .singleResult()
                .getProcessDefinitionId();
        // Get the BPMN model instance
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
        // Convert the BpmnModelInstance to a XML string
        String bpmnXml = Bpmn.convertToString(bpmnModelInstance);
        // 返回当前任务
        List<Task> taskList = taskService.createTaskQuery()
                .processInstanceId(dto.getProcessInstanceId())
                .active()
                .list();
        ActivityInstance activityInstance = runtimeService.getActivityInstance(dto.getProcessInstanceId());
        List<ProcessManagementDTO.TaskResultDTO> tasks = new ArrayList<>(taskList.size());
        if(CollectionUtil.isNotEmpty(taskList)){
            tasks = taskList.stream().map(task -> new ProcessManagementDTO.TaskResultDTO(task.getId(), task.getName(), task.getTaskDefinitionKey())).collect(Collectors.toList());
        }
        return new ProcessManagementDTO.ProcessResultDTO(tasks, bpmnXml, activityInstance.getProcessInstanceId(), activityInstance.getProcessDefinitionId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTaskHandle(DelegateTask task) {
        // 保存流程任务数据 execution 中包含实例信息,
        // 审批任务填充审批信息
        String processDefinitionId = task.getExecution().getProcessDefinitionId();
        String taskDefinitionKey = task.getTaskDefinitionKey();
        CamundaDTO.PropertiesDTO propertiesDTO = getProperties(taskDefinitionKey, processDefinitionId);
        if(null == propertiesDTO){
            log.warn("流程设计未配置扩展属性, processDefinitionId: {}, taskDefinitionKey: {}", processDefinitionId, taskDefinitionKey);
            return;
        }
        // 保存流程任务数据
        DelegateExecution processInstance = task.getExecution().getProcessInstance();
        String processInstanceId = processInstance.getId();
        String activityName = StrUtil.isNotBlank(processInstance.getCurrentActivityName()) ? processInstance.getCurrentActivityName():task.getName();
        String activityId = StrUtil.isNotBlank(processInstance.getCurrentActivityId()) ? processInstance.getCurrentActivityId():task.getTaskDefinitionKey();
        LocalDateTime processStartTime = LocalDateUtil.date2LocalDateTime(task.getCreateTime());
        String executionId = task.getExecutionId();
        // 查询流程设计审核人处理方式
        ProcessDefinitionEntity processDefinition = processDefinitionService.getById(processDefinitionId.split(":")[0]);
        if(null == processDefinition) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }
        List<String> candidateUsers = task.getCandidates().stream().map(IdentityLink::getUserId).collect(Collectors.toList());
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(candidateUsers);
        if(CollectionUtil.isEmpty(userList)){
            throw new ServiceException(ApiError.USER_NOT_EXIST);
        }
        Map<String, FindUserDTO> userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, e -> e ));
        String copyUser = propertiesDTO.getCopyUser();
        candidateUsers.forEach(userId -> {
            // 对去重类型做处理，自动审核通过
            FindUserDTO findUserDTO = userMap.get(userId);
            if(null == findUserDTO){
                return;
            }
            DictBasicEnum reviewSetting = processDefinition.getReviewSetting();
            Optional<ProcessTaskManagementEntity> processTaskManagement = Optional.empty();
            ProcessTaskManagementEntity insertTask = new ProcessTaskManagementEntity(processInstanceId, activityId, task.getId(), processStartTime, ApproveStatusEnum.APPROVE_ING, propertiesDTO, findUserDTO, executionId, activityName);
            ProcessTaskManagementEntity taskManagementEntity = processTaskManagementService.saveProcessTask(insertTask);

            if (StrUtil.isNotBlank(copyUser)){
                List<String> ccUserIds = Arrays.asList(copyUser.split(","));
                List<FindUserDTO> ccUserList = sysUserFeign.getUserListByUserIds(ccUserIds);
                processTaskCcService.saveCcUser(task.getId(), ccUserList, taskManagementEntity.getId());
            }

            // 审核人配置
            if (DictBasicEnum.ADJACENT_DEDUPE.equals(reviewSetting)) {
                // 相邻节点去重
                // 查询当前节点的上一个节点
                LinkedHashMap<String, List<ProcessTaskManagementEntity>> processTaskManagementList = processTaskManagementService.listHisByProcessInstanceId(processInstanceId, MathUtil.ONE);
                processTaskManagement = processTaskManagementList.entrySet().stream()
                        .findFirst()
                        .get()
                        .getValue().stream().filter(item -> item.getCurApproveId().equals(userId))
                        .findFirst();
            }else if(DictBasicEnum.GLOBAL_DEDUPE.equals(reviewSetting)) {
                // 全局去重
                // 查询已完成审核节点
                LinkedHashMap<String, List<ProcessTaskManagementEntity>> processTaskManagementList = processTaskManagementService.listHisByProcessInstanceId(processInstanceId, null);
                processTaskManagement = processTaskManagementList.values().stream().flatMap(List::stream)
                        .filter(item -> item.getCurApproveId().equals(userId))
                        .findFirst();
            }
            if (processTaskManagement.isPresent()) {
                // 审核人已存在，自动审核通过
                taskService.createComment(task.getId(), processInstanceId, "审核人重复,审核自动通过");
                taskService.complete(task.getId());
                updateApprove(processTaskManagement.get().getId(), ApproveTypeEnum.PASS, insertTask.getId(), processInstanceId, "审核人重复,审核自动通过");
            }
        });
    }

    @Override
    public List<ProcessManagementDTO.ManagementTaskDTO> listUnsendTask(String taskId, String timeoutStatus) {
        return baseMapper.listProcessTask(taskId, timeoutStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendTimeoutWarn(ProcessManagementDTO.ManagementTaskDTO task) {
        // 更新发送状态
        processTaskManagementService.updateTimeoutStatus(Collections.singletonList(task.getTaskManagementId()), TimeoutStatusEnum.SEND_WARN);
        if(task.getTaskStartTime().isEqual(task.getTimeoutWarnTime())){
            return;
        }
        // 发送超时提醒消息
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(new ArrayList<>(Collections.singletonList(task.getCurApproveId())));
        noticeMsgInfoDTO.setTitle("【流程管理中心】审批即将超时提醒");
        noticeMsgInfoDTO.setContent(StrUtil.format("**单据名称: **{}\n**审批开始时间：**{} \n您有一笔审批即将超时，请尽快处理！", task.getBusinessName(), task.getStartTime()));
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.FLW_TASK);
        // 默认tag请指定为msg_notice_default_tag，可以根据不同业务自行指定
        SendResult sendResult = mqProducerService.sendNoticeMsg(noticeMsgInfoDTO, Boolean.TRUE);
        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            log.error("发送超时提醒消息失败，失败原因：{}", JSONUtil.toJsonStr(sendResult));
            throw new ServiceException("发送超时提醒消息失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendTimeoutHandle(ProcessManagementDTO.ManagementTaskDTO task) {
        List<ProcessManagementDTO.ManagementTaskDTO> taskList = listUnsendTask(task.getTaskId(), TimeoutStatusEnum.UNSEND.getCode());
        if(CollectionUtil.isEmpty(taskList)){
            return;
        }
        // 更新发送状态
        List<String> taskManagementIds = taskList.stream().map(ProcessManagementDTO.ManagementTaskDTO::getTaskManagementId).collect(Collectors.toList());
        processTaskManagementService.updateTimeoutStatus(taskManagementIds, TimeoutStatusEnum.SEND_HANDLE);
        // 超时处理
        String taskId = task.getTaskId();
        // 查询当前任务
        Task taskEntity = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(null == taskEntity){
            throw new ServiceException(ApiError.ERROR_TASK_AUDIT_STATUS);
        }
        identityService.setAuthenticatedUserId(task.getCurApproveId());
        if(DictBasicEnum.TIMEOUT_HANDLING_ESCALATE.equals(task.getTimeoutHandleType())){
            // 转上级
            // 查询当前审批人上级
            List<String> curUserIds = taskList.stream().map(ProcessManagementDTO.ManagementTaskDTO::getCurApproveId).collect(Collectors.toList());
            List<UserSuperiorDTO> superiorList = sysUserFeign.listSuperiorByUserIds(curUserIds);
            if(CollectionUtil.isEmpty(superiorList)){
                log.error("当前审批人无上级，无法转上级处理，审批人：{} ", task.getCurApproveId());
                return;
            }
            Map<String, List<UserSuperiorDTO>> superiorMap = superiorList.stream()
                    .filter(item -> ChargeSuperiorEnum.DIRECT_DEPARTMENT_CHARGE.getCode().equals(item.getLevel()))
                    .collect(Collectors.groupingBy(UserSuperiorDTO::getUserId));
            // 多个任务对应同一个上级
            for (String superiorId : superiorMap.keySet()) {
                List<UserSuperiorDTO> userSuperiors = superiorMap.get(superiorId);
                if(CollectionUtil.isEmpty(userSuperiors)){
                    continue;
                }
                // 上级相同，直接转上级
                UserSuperiorDTO userSuperior = userSuperiors.get(0);
                taskService.delegateTask(taskEntity.getId(), userSuperior.getUserId());
                // 更新流程任务数据
                processTaskManagementService.updateTransfer(taskId, userSuperior.getUserId(),userSuperior.getUserName(), "", "【任务超时自动给上级】");
            }
        }else if(DictBasicEnum.TIMEOUT_HANDLING_REJECT_APPLICANT.equals(task.getTimeoutHandleType())){
            // 审核不通过
            runtimeService.createProcessInstanceModification(task.getProcessInstanceId())
                    //关闭相关任务
                    .cancelAllForActivity(taskEntity.getTaskDefinitionKey())
                    .setAnnotation("审批超时，自动驳回")
                    .execute();
            // 保存流程任务数据
            updateApprove(task.getTaskManagementId(), ApproveTypeEnum.REJECT, task.getManagementId(), task.getProcessInstanceId(), "审批超时，自动驳回");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean transferBatch(ProcessManagementDTO.TransferBatchDTO dto) {
        // 查询当前执行任务
        List<ProcessManagementDTO.ManagementTaskDTO> managementTasks  = listTaskById(dto.getIds());
        if(CollectionUtil.isEmpty(managementTasks)){
            throw new ServiceException(ApiError.TASK_NOT_EXIST);
        }
        for (ProcessManagementDTO.ManagementTaskDTO managementTask : managementTasks) {
            String taskId = managementTask.getTaskId();
            // 查询当前任务
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if(null == task){
                throw new ServiceException(ApiError.ERROR_TASK_AUDIT_STATUS);
            }
            // 转发任务给目标人员
            identityService.setAuthenticatedUserId(dto.getTargetUserId());
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getTargetUserId());
            if(null == findUserDTO){
                throw new ServiceException(ApiError.USER_NOT_EXIST);
            }
            taskService.delegateTask(task.getId(), dto.getTargetUserId());
            // 更新流程任务数据
            processTaskManagementService.updateTransfer(taskId, dto.getTargetUserId(),findUserDTO.getUserName(), managementTask.getCurApproveId(), dto.getRemark());
        }
        return Boolean.TRUE;
    }


    @Override
    public List<ProcessManagementDTO.ManagementTaskDTO> listTaskById(List<String> ids) {
        return baseMapper.listProcessTaskByIds(ids);
    }

    @Override
    public void completeTaskHandle(DelegateTask taskDelegate) {

    }
}
