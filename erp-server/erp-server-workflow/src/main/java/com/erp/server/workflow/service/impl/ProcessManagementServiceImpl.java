package com.erp.server.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.SqlConstants;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.DeduplicationUtil;
import com.common.core.utils.JsonPathUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.sys.dto.SysFeignDTO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.model.sys.enums.ChargeSuperiorEnum;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.handle.BaseWorkflowService;
import com.erp.sdk.fs.enmu.FsActionStatusEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.handler.MQSyncFsHandler;
import com.erp.server.workflow.listeners.CamundaGlobalListener;
import com.erp.server.workflow.mapper.ProcessManagementMapper;
import com.erp.server.workflow.service.*;
import io.netty.util.internal.StringUtil;
import io.seata.spring.annotation.GlobalTransactional;
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
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_MANAGEMENT;

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

    public static final String LAST_APPROVE_TYPE = "lastApproveType";
    public static final String LAST_APPROVE_TIME = "lastApproveTime";
    public static final String DELIVERY_DATE = "deliveryDate";
    public static final String LAST_COMMENT = "lastComment";
    public static final String LAST_APPROVER = "lastApprover";
    public static final String LAST_TASK_MANAGEMENT_ID = "lastTaskManagementId";
    public static final String APPROVE_TYPE = "approveType";
    // 流程管理服务
    @Resource
    private ProcessManagementService processManagementService;
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
    @Resource
    private WorkMenuService workMenuService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private CfgApproveSyncService cfgApproveSyncService;
    @Resource
    private CfgProcessService cfgProcessService;
    @Resource
    private CfgProcessRuleService cfgProcessRuleService;
    @Resource
    private CfgProcessExpService cfgProcessExpService;
    @Resource
    private SpElServer spElServer;
    @Resource
    private MQSyncFsHandler mqSyncFsHandler;
    @Resource
    private CfgQueryOptionService cfgQueryOptionService;
    @Resource
    private ApproveTaskInfoService approveTaskInfoService;
    @Resource
    private FsService fsService;
    @Resource
    private ThirdProcessManagementService thirdProcessManagementService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessManagementDTO.StartResultDTO startProcessManagement(ProcessManagementDTO.StartDTO dto) {
        CfgProcessRuleEntity cfgProcessRuleEntity = getProcessDefinitionId(dto);
        if (ObjectUtil.isEmpty(cfgProcessRuleEntity)) {
            // 业务无已启用的Erp流程配置
            return new ProcessManagementDTO.StartResultDTO(dto);
        }
        //启动飞书流程
        if (CharSequenceUtil.equals(cfgProcessRuleEntity.getType(),CfgProcessRuleTypeEnum.FSPROCESS.getCode())) {
            return startFsProcess(dto,cfgProcessRuleEntity);
        }
        //启动ERP流程
        return startProcess(dto, cfgProcessRuleEntity.getProcessDefinitionId());
    }


    /**
     * 查询流程定义id
     * @author will
     * @date 2025/5/19 16:10
     * @param dto
     * @return String
     */
    private CfgProcessRuleEntity getProcessDefinitionId(ProcessManagementDTO.StartDTO dto) {
        //查询流程配置
        CfgProcessEntity cfgProcessEntity = cfgProcessService.getByBusinessKey(dto.getBusinessKey());
        if (ObjectUtil.isEmpty(cfgProcessEntity)) {
            // 业务无流程配置
            log.warn("业务无流程配置, businessKey={}", dto.getBusinessKey());
            return null;
        }
        List<CfgProcessRuleEntity> cfgProcessRuleList = cfgProcessRuleService.listByProcessId(cfgProcessEntity.getId(),"");
        if (CollUtil.isEmpty(cfgProcessRuleList)) {
            // 业务无已启用的Erp流程配置
            log.warn("业务无已启用的Erp流程配置, businessKey={}", dto.getBusinessKey());
            return null;
        }
        //查询rule条件设置
        List<String> ruleIdList = cfgProcessRuleList.stream().map(CfgProcessRuleEntity::getId).distinct().collect(Collectors.toList());
        List<CfgProcessExpEntity> cfgProcessExpList = cfgProcessExpService.listByRuleIdList(ruleIdList);
        if (CollUtil.isEmpty(cfgProcessExpList)) {
            // 业务无已启用的Erp流程配置
            log.warn("业务无规则对应的条件设置, businessKey={}", dto.getBusinessKey());
            //存在一条以上的规则都匹配数据的时候直接报错
            if (cfgProcessRuleList.size() > MathUtil.ONE) {
                throw new ServiceException(ApiError.PROCESS_RULE_REPEAT_ERROR,SourceTypeEnum.getName(cfgProcessEntity.getBussinessKey()));
            }
            return cfgProcessRuleList.get(0);
        }
        Map<String, List<CfgProcessExpEntity>> expMap = cfgProcessExpList.stream().collect(Collectors.groupingBy(CfgProcessExpEntity::getRuleId));
        //查询传入数据是否有符合条件的流程
        List<CfgProcessRuleEntity> processRuleList = new ArrayList<>();
        for (CfgProcessRuleEntity cfgProcessRuleEntity : cfgProcessRuleList) {
            //对应规则,未发现规则则直接通过
            List<CfgProcessExpEntity> processExpList = expMap.get(cfgProcessRuleEntity.getId());
            if (CollUtil.isEmpty(processExpList)) {
                processRuleList.add(cfgProcessRuleEntity);
            } else {
                List<ConditionElement> conditionElementList = BeanMapper.copyList(processExpList, ConditionElement.class);
                List<CfgQueryOptionEntity> cfgQueryOptionEntities = cfgQueryOptionService.list(new LambdaQueryWrapper<CfgQueryOptionEntity>().eq(CfgQueryOptionEntity::getBussinessKey, cfgProcessEntity.getBussinessKey()).eq(CfgQueryOptionEntity::getIsDeleted, false));
                Set<String> keySet = cfgQueryOptionEntities.stream().map(CfgQueryOptionEntity::getFieldBelongsType).collect(Collectors.toSet());
                Boolean matchResult = false;
                for (String key : keySet) {
                    if (spElServer.matchExpressionByConditionList(conditionElementList, dto.getVariablesMap(), key)) {
                        matchResult = true;
                        break;
                    }
                }
                //匹配上则直接赋值
                if (Boolean.TRUE.equals(matchResult)) {
                    processRuleList.add(cfgProcessRuleEntity);
                }
            }
        }
        if (CollUtil.isEmpty(processRuleList)) {
            // 业务无匹配规则的Erp流程配置
            log.warn("业务无已启用的Erp流程配置, businessKey={}", dto.getBusinessKey());
            return null;
        }
        //存在一条以上的规则都匹配数据的时候直接报错
        if (CollUtil.isNotEmpty(processRuleList) && processRuleList.size() > MathUtil.ONE) {
            throw new ServiceException(ApiError.PROCESS_RULE_REPEAT_ERROR,SourceTypeEnum.getName(cfgProcessEntity.getBussinessKey()));
        }
        return processRuleList.get(0);
    }


    /**
     * 启动流程
     * @author will
     * @date 2025/5/19 15:20
     * @param dto
     * @return StartResultDTO
     */
    private ProcessManagementDTO.StartResultDTO startProcess(ProcessManagementDTO.StartDTO dto,String processDefinitionId) {
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
        // 查询流程定义
        ProcessDefinitionEntity processDefinition = processDefinitionService.getIsDeployEntityById(processDefinitionId);
        if (ObjectUtil.isEmpty(processDefinition) || processDefinition.getDisabled()) {
            log.warn("为找到流程定义, processDefinitionId={}", processDefinitionId);
            return new ProcessManagementDTO.StartResultDTO(dto);
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
        ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
        ExecutionEntity executionEntity = ((ProcessInstanceWithVariablesImpl) processInstance).getExecutionEntity();
        ActivityImpl activity = executionEntity.getActivity();
        String taskId;
        String activityId;
        if (ObjectUtil.isEmpty(activity)) {
            // 多实例节点 获取当前活动节点方法
            ActivityInstance[] childActivityInstances = activityInstance.getChildActivityInstances();
            if (ObjectUtil.isEmpty(childActivityInstances) || childActivityInstances.length == 0) {
                log.error("流程实例[{}]没有多实例子节点", processInstance.getId());
                throw new ServiceException(ApiError.ERROR_94004);
            }
            activityId = childActivityInstances[0].getActivityId();
            activityId = activityId.contains("#") ? activityId.substring(0, activityId.indexOf("#")) : activityId;
            List<ExecutionEntity> executions = executionEntity.getExecutions();
            if (CollectionUtils.isEmpty(executions)) {
                log.error("流程实例[{}]没有多实例执行任务", processInstance.getId());
                throw new ServiceException(ApiError.ERROR_94004);
            }
            List<TaskEntity> tasks = executions.get(0).getTasks();
            if (CollectionUtils.isEmpty(tasks)) {
                List<ExecutionEntity> executionChild = executions.get(0).getExecutions();
                if (CollectionUtils.isEmpty(executionChild)) {
                    log.error("流程实例[{}]没有多实例执行子任务", processInstance.getId());
                    throw new ServiceException(ApiError.ERROR_94004);
                }
                tasks = executionChild.get(0).getTasks();
            }
            if (CollectionUtils.isEmpty(tasks)) {
                log.error("流程实例[{}]没有多实例执行任务列表为空", processInstance.getId());
                throw new ServiceException(ApiError.ERROR_94004);
            }
            taskId = tasks.get(0).getId();
        } else {
            activityId = activity.getActivityId();
            taskId = executionEntity.getTasks().get(0).getId();
        }
        String processInstanceId = processInstance.getProcessInstanceId();
        // 保存审批节点数据
        ProcessManagementEntity insertManagementEntity = new ProcessManagementEntity(processInstanceId, dto, activityId, processStartTime, processDefinition, processInstance.getProcessDefinitionId());
        if (!save(insertManagementEntity)) {
            // 保存流程数据失败
            throw new ServiceException(ApiError.ERROR_94004);
        }

        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //判断该单据类型是否有ERP审批同步定义
                CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
                mqDto.setProcessManagementId(insertManagementEntity.getId());
                mqDto.setBusinessName(insertManagementEntity.getBusinessName());
                mqDto.setBusinessCode(insertManagementEntity.getBusinessCode());
                mqDto.setInstanceId(processInstanceId);
                mqDto.setCurTaskId(taskId);
                mqDto.setOperator(dto.getUserId());
                mqDto.setVariablesMap(variables);
                mqDto.setBusinessKey(dto.getBusinessKey());
                syncFsExternalInstance(mqDto);
            }
        });
        return new ProcessManagementDTO.StartResultDTO(processDefinitionId, processInstanceId, taskId, processStartTime, dto.getBusinessId(), dto.getBusinessName());
    }

    /**
     * 启动飞书流程
     */
    private  ProcessManagementDTO.StartResultDTO startFsProcess(ProcessManagementDTO.StartDTO dto,CfgProcessRuleEntity cfgProcessRuleEntity) {

            if (null == cfgProcessRuleEntity){
                // 流程定义不存在
                throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
            }
            String ruleId = cfgProcessRuleEntity.getId();
            CfgProcessDTO.StartDTO startDTO = BeanUtil.copyProperties(dto, CfgProcessDTO.StartDTO.class);
            //TODO 获取当前用户
            startDTO.setUserId(UserContext.getLoginUser().getUid());
            startDTO.setRuleId(ruleId);
            startDTO.setRuleType(cfgProcessRuleEntity.getType());
            log.info("startDTO重要标识:{}",startDTO.toString());
            cfgProcessService.startThirdProcess(startDTO);
            return new ProcessManagementDTO.StartResultDTO(cfgProcessRuleEntity.getProcessDefinitionId(), "", "", LocalDateTime.now(), dto.getBusinessId(), dto.getBusinessName());
    }

    /**
     * 审批任务填充审批信息
     *
     * @param startUserId
     * @param propertiesDTO
     */
    private List<String> addApproveInfo(String startUserId, CamundaDTO.PropertiesDTO propertiesDTO, Map<String, Object> variables) {
        // 获取审批人
        String candidateUsers = propertiesDTO.getCandidateUsers();
        // 使用策略模式获取审批人
        List<String> userIds = assigneeStrategyService.getResult(propertiesDTO, startUserId, candidateUsers, variables);
        // 无审批人处理
        if (CollectionUtils.isEmpty(userIds)) {
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
        Query<CamundaProperties> camundaPropertiesQuery = extensionElements.getElementsQuery().filterByType(CamundaProperties.class);
        if (camundaPropertiesQuery.count() <= 0) {
            return null;
        }
        Collection<CamundaProperty> camundaProperties = camundaPropertiesQuery
                .singleResult()
                .getCamundaProperties();
        if (CollectionUtils.isEmpty(camundaProperties)) {
            return null;
        }
        Map<String, String> propertiesMap = camundaProperties.stream()
                .collect(Collectors.toMap(CamundaProperty::getCamundaName, value -> CharSequenceUtil.isNotBlank(value.getCamundaValue()) ? value.getCamundaValue() : ""));
        return BeanUtil.toBean(propertiesMap, CamundaDTO.PropertiesDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessManagementDTO.ApproveResultDTO approveProcess(ProcessManagementDTO.ApproveDTO dto,Boolean isFirst) {
        //判断是否走飞书流程
       Boolean isFsApprove = isFsApprovePass(dto.getBusinessId(),dto.getBusinessKey());
       if (isFsApprove) {
           throw new ServiceException(ApiError.PROCESS_APPROVE_FS_PROCESS);
       }

        log.info("流程审批：{}", JSONUtil.toJsonStr(dto));
        List<ProcessManagementEntity> processManagementList = listByBusiness(dto.getBusinessKey(), dto.getBusinessId());
        if (CollectionUtils.isEmpty(processManagementList)) {
            // 业务未启动流程
            return new ProcessManagementDTO.ApproveResultDTO(dto);
        }

        // 查询流程数据 , dto.getUserId()
        ProcessManagementDTO.ManagementTaskDTO managementTask = getCurApproveTask(dto.getBusinessId(), dto.getBusinessKey(), dto.getUserId());
        if (!ProcessStatusEnum.RUNNING.equals(managementTask.getProcessStatus())) {
            throw new ServiceException(ApiError.PROCESS_MANAGEMENT_PROCESS_STATUS_ERROR,managementTask.getProcessStatus().getName());
        }

        // 审核操作
        // 获取当前任务
        Task currentTask = taskService.createTaskQuery().taskId(managementTask.getTaskId()).singleResult();
        if(null == currentTask) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NODE_NOT_EXIST);
        }
        String processInstanceId = managementTask.getProcessInstanceId();

        //添加审批意见
        identityService.setAuthenticatedUserId(dto.getUserId());
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        variables.put(LAST_APPROVE_TYPE, dto.getApproveType().getStatus());
        variables.put(LAST_APPROVE_TIME, LocalDateTime.now());
        variables.put(LAST_COMMENT, dto.getComment());
        variables.put(LAST_APPROVER, dto.getUserId());
        variables.put(LAST_TASK_MANAGEMENT_ID, managementTask.getTaskManagementId());
        runtimeService.setVariables(processInstanceId, variables);
        if(ApproveTypeEnum.PASS.equals(dto.getApproveType())) {
            // 审核通过
            taskService.createComment(managementTask.getTaskId(), processInstanceId, dto.getComment());
            Map<String, Object> variablesMap = dto.getVariablesMap();
            variablesMap.put(APPROVE_TYPE, dto.getApproveType().getStatus());
            try {
                taskService.complete(managementTask.getTaskId(), variablesMap);
            } catch (Exception e) {
                log.error("审核失败，msg ={}",e.getMessage());
                throw new ServiceException(ApiError.ERROR_TASK_COMPLETE_FAIL,e.getMessage());
            }
        }
        if(ApproveTypeEnum.REJECT.equals(dto.getApproveType())) {
            // 审核不通过
            runtimeService.createProcessInstanceModification(processInstanceId)
                    //关闭相关任务
                    .cancelAllForActivity(currentTask.getTaskDefinitionKey())
                    .setAnnotation(dto.getComment())
                    .execute();
        }
        // 保存流程任务数据
        processManagementService.updateApprove(managementTask.getTaskManagementId(), dto.getApproveType(), managementTask.getManagementId(), processInstanceId,dto.getComment(), dto.getVariablesMap());
        if(Boolean.TRUE.equals(isFirst)){
            sameApproverAutoPass(dto, processManagementList.get(0).getProcessDefinitionId(),currentTask.getProcessInstanceId());
        }

        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //判断该单据类型是否有ERP审批同步定义
                CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
                mqDto.setProcessManagementId(managementTask.getManagementId());
                mqDto.setBusinessName(managementTask.getBusinessName());
                mqDto.setBusinessCode(managementTask.getBusinessCode());
                mqDto.setInstanceId(processInstanceId);
                mqDto.setCurTaskId(managementTask.getTaskId());
                mqDto.setOperator(dto.getUserId());
                mqDto.setVariablesMap(variables);
                mqDto.setBusinessKey(dto.getBusinessKey());
                mqDto.setApproveType(dto.getApproveType().getStatus());
                syncFsExternalInstance(mqDto);
            }
        });
        // 返回结果
        return new ProcessManagementDTO.ApproveResultDTO(currentTask.getProcessDefinitionId(), currentTask.getProcessInstanceId(), managementTask.getBusinessId(), managementTask.getBusinessName(),currentTask.getId(),currentTask.getName(), currentTask.getTaskDefinitionKey());
    }

    /**
     * 判断单据是否走飞书审批
     * @author will
     * @date 2025/6/27 17:40
     * @param businessId
     * @param businessKey
     * @return Boolean
     */
    private Boolean isFsApprovePass (String businessId,String businessKey) {
        //查询三方审批生成记录
        ApproveTaskInfoEntity approveTaskInfo = approveTaskInfoService.  getByBusinessIdAndKey(businessId, businessKey);
        if (ObjectUtil.isEmpty(approveTaskInfo)) {
            return  Boolean.FALSE;
        }
        //查询最后一条第三方流程管理记录，如果已结束则返回false
        ThirdProcessManagementEntity managementEntity = thirdProcessManagementService.getLastByBusinessIdAndKey(businessId, businessKey);
        if (ObjectUtil.isNotEmpty(managementEntity) && ObjectUtil.isNotEmpty(managementEntity.getEndTime())) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 飞书三方审批实例同步
     * @author jack
     * @date 2025-05-21
     */
    private void syncFsExternalInstance(CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto) {
        //判断该单据类型是否有ERP审批同步定义
        List<CfgApproveSyncEntity> cfgApproveSyncEntities = cfgApproveSyncService.getByBusinessType(Arrays.asList(mqDto.getBusinessKey()))
                .stream()
                .filter(e -> e.getEnableStatus().equals(Boolean.TRUE))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(cfgApproveSyncEntities)) {
            CfgApproveSyncEntity cfgApproveSyncEntity = cfgApproveSyncEntities.get(0);
            mqDto.setCfgApproveSyncEntity(cfgApproveSyncEntity);
            mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.WORKFLOW_SYNC_FS_INSTANCE_TOPIC, RocketMqTagEnum.WORKFLOW_SYNC_FS_INSTANCE_TAG.getName(),mqDto , IdUtil.simpleUUID(),1);
        }
    }

    //    @Transactional(rollbackFor = Exception.class)
    public void sameApproveHandler(ProcessManagementDTO.ApproveDTO dto, ProcessManagementDTO.ManagementTaskDTO managementTask, DictBasicEnum reviewSetting) {
        String userId = dto.getUserId();
        String processInstanceId = managementTask.getProcessInstanceId();
        // 审核人配置
        if (DictBasicEnum.ADJACENT_DEDUPE.equals(reviewSetting)) {
            // 查询上级节点审批人
            List<ProcessTaskManagementEntity> processTaskManagementList = processTaskManagementService.listPreActivityTask(managementTask.getTaskManagementId(), processInstanceId);
            ProcessTaskManagementEntity processTaskManagement = null;
            if (!CollectionUtils.isEmpty(processTaskManagementList)){
                Optional<ProcessTaskManagementEntity> first = processTaskManagementList.stream().filter(x -> x.getApproveId().equals(managementTask.getCurApproveId())).findFirst();
                processTaskManagement = first.orElse(null);
            }
            if (!userId.equals(managementTask.getCurApproveId()) && null == processTaskManagement) {
                return;
            }
            dto.setComment("相邻节点审核人重复,审核自动通过");
            dto.setUserId(managementTask.getCurApproveId());
            processManagementService.approveProcess(dto,Boolean.FALSE);
        }else if(DictBasicEnum.GLOBAL_DEDUPE.equals(reviewSetting)) {
            // 全局去重
            // 查询已完成审核节点
            LinkedHashMap<String, List<ProcessTaskManagementEntity>> processTaskManagementMap = processTaskManagementService.listHisByProcessInstanceId(processInstanceId, null);
            if(CollectionUtils.isEmpty(processTaskManagementMap)){
                return;
            }
            // 存在已审核节点
            Optional<ProcessTaskManagementEntity> processTaskManagement = processTaskManagementMap.values().stream().flatMap(List::stream)
                        .filter(item -> item.getApproveId().equals(managementTask.getCurApproveId()))
                        .findFirst();
            if(!processTaskManagement.isPresent()) {
                return;
            }
            dto.setComment("全局去重审核人重复,审核自动通过");
            dto.setUserId(managementTask.getCurApproveId());
            processManagementService.approveProcess(dto, Boolean.FALSE);
        }
    }

    private List<ProcessManagementDTO.ManagementTaskDTO> getCurApproveTaskList(String businessId, String businessKey, String userId) {
        List<ProcessManagementDTO.ManagementTaskDTO> managementTaskDTOS = listTaskByBusiness(businessId, businessKey);
        if(CollectionUtils.isEmpty(managementTaskDTOS)){
            return Collections.emptyList();
        }
        if(CharSequenceUtil.isBlank(userId)){
            return managementTaskDTOS;
        }
        // 审核人校验
        return managementTaskDTOS.stream()
                // 过滤当前审核人
                .filter(managementTaskDTO -> managementTaskDTO.getCurApproveId().equals(userId))
                // 过滤重复的执行id
                .filter(DeduplicationUtil.distinctByKey(ProcessManagementDTO.ManagementTaskDTO::getExecutionId))
                .collect(Collectors.toList());
    }

    /**
     * 查询当前审批任务
     * @param businessId
     * @param businessKey
     * @param userId
     * @return
     */
    private ProcessManagementDTO.ManagementTaskDTO getCurApproveTask(String businessId, String businessKey, String userId) {
        List<ProcessManagementDTO.ManagementTaskDTO> managementTaskDTOS = listTaskByBusiness(businessId, businessKey);
        if (CollectionUtils.isEmpty(managementTaskDTOS)) {
            throw new ServiceException(ApiError.PROCESS_ALREADY_END);
        }
        // 审核人校验
        return managementTaskDTOS.stream()
                .filter(managementTaskDTO -> managementTaskDTO.getCurApproveId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_95049));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateApprove(String taskId, ApproveTypeEnum approveType, String managementId, String processInstanceId, String comment, @Nullable Map<String, Object> variablesMap) {
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
        if (!update){
            log.error("更新流程管理失败:{}", managementId);
        }

        // 更新流程任务数据
        ProcessManagementEntity managementEntity = getById(managementId);
        processTaskManagementService.updateApprove(taskId, approveType, comment, "", managementEntity, variablesMap);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> getCandidateByAct(PvmActivity act, DelegateExecution executionDelegate, String startUserId) {
        String processDefinitionId = executionDelegate.getProcessDefinitionId();
        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
        // 获取当前节点的 CamundaProperty
        String actId = act.getId();
        actId =actId.contains("#") ? actId.substring(0, actId.indexOf("#")) : actId;
        FlowElement flowElement = modelInstance.getModelElementById(actId);
        // 获取当前节点的 CamundaProperty
        ExtensionElements extensionElements = flowElement.getExtensionElements();
        if(null == extensionElements){
            log.warn("流程设计未配置扩展属性, processDefinitionId: {}", processDefinitionId);
            return Collections.emptyList();
        }
        Query<CamundaProperties> camundaPropertiesQuery = extensionElements.getElementsQuery().filterByType(CamundaProperties.class);
        if (camundaPropertiesQuery.count() <= 0) {
            log.warn("流程设计未配置扩展属性, processDefinitionId: {}", processDefinitionId);
            return Collections.emptyList();
        }
        Collection<CamundaProperty> camundaProperties = camundaPropertiesQuery.singleResult().getCamundaProperties();
        Map<String, String> propertiesMap = camundaProperties.stream()
                .collect(Collectors.toMap(CamundaProperty::getCamundaName, value -> CharSequenceUtil.isNotBlank(value.getCamundaValue()) ? value.getCamundaValue() : ""));
        CamundaDTO.PropertiesDTO propertiesDTO = BeanUtil.toBean(propertiesMap, CamundaDTO.PropertiesDTO.class);
        // 获取当前节点的候选人
        Map<String, Object> variables = executionDelegate.getVariables();
        List<String> candidateUsers = addApproveInfo(startUserId, propertiesDTO, variables);
        if(CollectionUtils.isEmpty(candidateUsers)){
            log.warn("任务节点无审批人为空 startUserId={}", startUserId);
            // 无审批人终止流程
            throw new ServiceException(ApiError.PROCESS_NOT_APPROVER);
        }
        return candidateUsers;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessManagementDTO.BackResultDTO back(ProcessManagementDTO.BackDTO dto) {
        // 查询业务数据和关联流程定义
        List<ProcessManagementEntity> processManagementList = listByBusiness(dto.getBusinessKey(), dto.getBusinessId());
        if (CollectionUtils.isEmpty(processManagementList)) {
            // 业务未启动流程
            return new ProcessManagementDTO.BackResultDTO(dto);
        }

        // 查询流程数据
        ProcessManagementDTO.ManagementTaskDTO managementTask = getCurApproveTask(dto.getBusinessId(), dto.getBusinessKey(), dto.getUserId());
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
        if(CollectionUtils.isEmpty(historyActivityList)){
            throw new ServiceException(ApiError.PROCESS_TASK_NOT_REJECT);
        }
        HistoricActivityInstance historicActivityInstance = historyActivityList.get(0);
        ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);
        if(ApproveTypeEnum.REJECT_APPOINT.equals(dto.getApproveType())) {
            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
            variables.put(APPROVE_TYPE, ApproveTypeEnum.REJECT_APPOINT.getStatus());
            variables.put(LAST_APPROVE_TIME, LocalDateTime.now());
            variables.put(LAST_COMMENT, dto.getComment());
            variables.put(LAST_APPROVER, dto.getUserId());
            runtimeService.setVariables(processInstanceId, variables);
            // 驳回指定节点
            runtimeService
                    .createProcessInstanceModification(processInstanceId)
                    .cancelActivityInstance(activityInstance.getId())
                    .setAnnotation(CharSequenceUtil.format("驳回到指定节点", managementTask.getBusinessName()))
                    //启动目标活动节点
                    .startBeforeActivity(historicActivityInstance.getActivityId())
                    //流程的可变参数赋值
                    .setVariables(dto.getVariablesMap())
                    .execute();
            // 保存流程任务数据
            backUpdateApprove(managementTask.getTaskManagementId(), managementTask.getManagementId(), dto.getApproveType(),dto.getActivityId(), dto.getComment());
        }
        return new ProcessManagementDTO.BackResultDTO(historicActivityInstance.getActivityId(), historicActivityInstance.getActivityName(),managementTask);
    }

    @Override
    public ProcessManagementDTO.ManagementTaskDTO getTaskByBusiness(String businessId, String businessKey, String userId) {
        return baseMapper.getTaskByBusiness(businessId, businessKey, userId);
    }

    /**
     * 查询当前审批任务列表
     * @param businessId
     * @param businessKey
     * @return
     */
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
        processTaskManagementService.updateApprove(taskId, approveType, comment, activityId, managementEntity, null);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean transfer(ProcessManagementDTO.TransferDTO dto) {
        // 查询当前执行任务
        ProcessManagementDTO.ManagementTaskDTO managementTask = getCurApproveTask(dto.getBusinessId(), dto.getBusinessKey(), dto.getSourceUserId());
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

        Map<String, Object> variables = runtimeService.getVariables(managementTask.getProcessInstanceId());
        //操作人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //判断该单据类型是否有ERP审批同步定义
                CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
                mqDto.setProcessManagementId(managementTask.getManagementId());
                mqDto.setBusinessName(managementTask.getBusinessName());
                mqDto.setBusinessCode(managementTask.getBusinessCode());
                mqDto.setInstanceId(managementTask.getProcessInstanceId());
                mqDto.setCurTaskId(managementTask.getTaskId());
                mqDto.setOperator(userInfo.getUid());
                mqDto.setVariablesMap(variables);
                mqDto.setBusinessKey(managementTask.getBusinessKey());
                mqDto.setApproveType(FsActionStatusEnum.FORWARDED.getCode());//转办
                syncFsExternalInstance(mqDto);
            }
        });
        return Boolean.TRUE;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessManagementDTO.RevokeResultDTO revoke(ProcessManagementDTO.RevokeDTO dto) {
        //判断是否走飞书流程
        Boolean isFsApprove = isFsApprovePass(dto.getBusinessId(),dto.getBusinessKey());
        if (isFsApprove) {
            throw new ServiceException(ApiError.PROCESS_APPROVE_FS_PROCESS);
        }

        // 查询业务数据和关联流程定义
        List<ProcessManagementEntity> processManagementList = listByBusiness(dto.getBusinessKey(), dto.getBusinessId());
        if (CollectionUtils.isEmpty(processManagementList)) {
            // 业务未启动流程
            return new ProcessManagementDTO.RevokeResultDTO(dto);
        }
        // 查询流程实例
        List<ProcessManagementDTO.ManagementTaskDTO> managementTaskDTOS = listTaskByBusiness(dto.getBusinessId(), dto.getBusinessKey());
        if (CollectionUtils.isEmpty(managementTaskDTOS)) {
            throw new ServiceException(ApiError.PROCESS_ALREADY_END);
        }
        ProcessManagementDTO.ManagementTaskDTO managementTask = managementTaskDTOS.get(0);
        String managementCreateUserId = managementTask.getManagementCreateUserId();
        if (!CharSequenceUtil.equals(managementCreateUserId, dto.getUserId())) {
            throw new ServiceException(ApiError.PROCESS_NOT_START_USER);
        }

        String processInstanceId = managementTask.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        // 查询当前实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
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
        // 任务完成时的逻辑处理
        for (Execution execution : executions) {
            if (execution instanceof ExecutionEntity) {
                ExecutionEntity executionEntity = (ExecutionEntity) execution;
                // 根据节点状态进行撤回操作
                if (executionEntity.isActive() && !executionEntity.isEnded() && !executionEntity.getActivityId().equals(initialActivityId)) {
                    runtimeService.deleteProcessInstance(execution.getProcessInstanceId(), "process revoke", true);
                    historyService.deleteHistoricProcessInstance(execution.getProcessInstanceId());
                    break;
                }
            }
        }

        //判断该单据类型是否有ERP审批同步定义
        CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
        mqDto.setProcessManagementId(managementTask.getManagementId());
        mqDto.setBusinessName(managementTask.getBusinessName());
        mqDto.setBusinessCode(managementTask.getBusinessCode());
        mqDto.setInstanceId(processInstanceId);
        mqDto.setCurTaskId(managementTask.getTaskId());
        mqDto.setOperator(dto.getUserId());
        mqDto.setVariablesMap(variables);
        mqDto.setBusinessKey(dto.getBusinessKey());
        mqDto.setApproveType(ApproveTypeEnum.CANCEL.getStatus());//撤销
        //判断该单据类型是否有ERP审批同步定义
        List<CfgApproveSyncEntity> cfgApproveSyncEntities = cfgApproveSyncService.getByBusinessType(Arrays.asList(mqDto.getBusinessKey()))
                .stream()
                .filter(e -> e.getEnableStatus().equals(Boolean.TRUE))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(cfgApproveSyncEntities)) {
            CfgApproveSyncEntity cfgApproveSyncEntity = cfgApproveSyncEntities.get(0);
            mqDto.setCfgApproveSyncEntity(cfgApproveSyncEntity);
            mqSyncFsHandler.handler(mqDto);
        }
        // 删除本地流程任务数据
        removeByProcessInstanceId(processInstance.getProcessInstanceId());

//        // 完成新增数据事务提交之后,发送MQ消息
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCommit() {
//                //判断该单据类型是否有ERP审批同步定义
//                CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
//                mqDto.setProcessManagementId(managementTask.getManagementId());
//                mqDto.setBusinessName(managementTask.getBusinessName());
//                mqDto.setBusinessCode(managementTask.getBusinessCode());
//                mqDto.setInstanceId(processInstanceId);
//                mqDto.setCurTaskId(managementTask.getTaskId());
//                mqDto.setOperator(dto.getUserId());
//                mqDto.setVariablesMap(variables);
//                mqDto.setBusinessKey(dto.getBusinessKey());
//                mqDto.setApproveType(ApproveTypeEnum.CANCEL.getStatus());//撤销
//                syncFsExternalInstance(mqDto);
//            }
//        });
        return new ProcessManagementDTO.RevokeResultDTO(processInstance.getProcessDefinitionId(), processInstance.getProcessInstanceId(), managementTask.getBusinessId(), managementTask.getBusinessName());
    }

    /**
     * 撤回飞书审批
     * @param dto
     * @return
     */
    private ProcessManagementDTO.RevokeResultDTO revokeThirdInstance(ProcessManagementDTO.RevokeDTO dto) {
        ApproveTaskInfoEntity taskInfo = null;
        try {
            //查询三方生成查询记录
             taskInfo = approveTaskInfoService.getByBusinessIdAndKey(dto.getBusinessKey(),dto.getBusinessId());
        }catch (Exception e){
            throw new ServiceException(ApiError.PROCESS_APPROVE_TASK_INFO_ERROR);
        }
            //判断是否走飞书审批
            if (ObjectUtil.isNotEmpty(taskInfo)) {
                String approvalCode = taskInfo.getThirdApprovalCode();
                String thirdInstanceCode = taskInfo.getThirdInstanceId();

                if (StrUtil.isNotEmpty(dto.getUserId()) && StrUtil.isNotEmpty(taskInfo.getCreateUserId())) {
                    if (!taskInfo.getCreateUserId().equals(dto.getUserId())) {
                        throw new ServiceException(ApiError.PROCESS_NOT_START_USER);
                    }
                    //查询用户
                    List<ThirdUnionDTO> thirdUsers = sysUserFeign.getThirdByUserIds(ProcessSourcePlatformEnum.FS.getCode().toUpperCase(), Collections.singletonList(dto.getUserId()));
                    if (thirdUsers.size() > 1){
                        throw new ServiceException(ApiError.PROCESS_QUERY_THIRD_SUER_MULTIPLE);
                    }
                    if (thirdUsers.size() == 0 || StrUtil.isEmpty(thirdUsers.get(0).getThirdUserId())){
                        throw new ServiceException(ApiError.PROCESS_QUERY_THIRD_SUER_NOT_EXIST);
                    }
                    for (ThirdUnionDTO thirdUser : thirdUsers) {
                        //这里需要判断thirdUnionId;thirdUserId;thirdOpenId;是否为空，哪个不为空，用哪个
                        String userId = thirdUser.getThirdUserId() != null
                                ? thirdUser.getThirdUserId()
                                : thirdUser.getThirdOpenId() != null
                                ? thirdUser.getThirdOpenId()
                                : thirdUser.getThirdUnionId();
                        try {
                            Boolean revoke = fsService.revoke(approvalCode, thirdInstanceCode, userId);
                            if (revoke){
                                return new ProcessManagementDTO.RevokeResultDTO(dto);
                            }
                        }catch (Exception e){
                            throw new ServiceException(e.getMessage());
                        }
                    }
                }
            }
        return null;
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
        if(CollectionUtils.isEmpty(taskList)){
            return Collections.emptyList();
        }
        return taskList.stream()
                .map(ProcessManagementDTO.HistoryActivityResultDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public PagingVO<ProcessManagementDTO.PagingResultDTO> paging(PagingDTO<ProcessManagementDTO.SearchDTO> pageDTO) {
        pageDTO.getParams().setPermissionSql(pageDTO.getPermissionSql());
        // 查询流程实例
        Page<ProcessManagementDTO.PagingResultDTO> query = new Page<>(pageDTO.getCurrPage(), pageDTO.getPageSize());
        IPage<ProcessManagementDTO.PagingResultDTO> pageData = baseMapper.paging(query, pageDTO.getParams());
        pageData.getRecords().forEach(record -> {
            if (record.getProcessStatus() != null) {
                record.setProcessStatusName(record.getProcessStatus().getName());
            }
            if (record.getTaskStatus() != null) {
                record.setTaskStatusName(record.getTaskStatus().getName());
            }
            if (record.getSourcePlatform() != null) {
                record.setSourcePlatformName(ProcessSourcePlatformEnum.getName(record.getSourcePlatform()));
            }
            record.setBusinessKeyName(SourceTypeEnum.getName(record.getBusinessKey()));
        });
        return new PagingVO<>(pageData);
    }

    @Override
    public void export(ProcessManagementDTO.ExportDTO dto) {
        downloadTaskFeign.saveExportTask("流程管理", EXPORT_PROCESS_MANAGEMENT.getCode(), dto);
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
        if(!CollectionUtils.isEmpty(taskList)){
            tasks = taskList.stream().map(task -> new ProcessManagementDTO.TaskResultDTO(task.getId(), task.getName(), task.getTaskDefinitionKey())).collect(Collectors.toList());
        }
        return new ProcessManagementDTO.ProcessResultDTO(tasks, bpmnXml, activityInstance.getProcessInstanceId(), activityInstance.getProcessDefinitionId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTaskHandle(DelegateTask task) {
        // 保存流程任务数据 execution 中包含实例信息,
        // 审批任务填充审批信息
        DelegateExecution execution = task.getExecution();
        String processDefinitionId = execution.getProcessDefinitionId();
        String taskDefinitionKey = task.getTaskDefinitionKey();
        // 获取扩展属性
        CamundaDTO.PropertiesDTO propertiesDTO = getProperties(taskDefinitionKey, processDefinitionId);
        if(null == propertiesDTO){
            log.warn("流程设计未配置扩展属性, processDefinitionId: {}, taskDefinitionKey: {}", processDefinitionId, taskDefinitionKey);
            return;
        }
        // 保存流程任务数据
        saveProcessTaskData(task, execution, propertiesDTO);
    }

    private void saveProcessTaskData(DelegateTask task, DelegateExecution execution, CamundaDTO.PropertiesDTO propertiesDTO) {
        DelegateExecution processInstance = task.getExecution().getProcessInstance();
        String processInstanceId = processInstance.getId();
        // 获取流程实例信息
        String activityName = getActivityName(processInstance, task);
        String activityId = getActivityId(processInstance, task);
        LocalDateTime processStartTime = LocalDateUtil.date2LocalDateTime(task.getCreateTime());
        String executionId = task.getExecutionId();
        String processDefinitionId = execution.getProcessDefinitionId();

        // 查询流程设计审核人处理方式
        ProcessDefinitionEntity processDefinition = processDefinitionService.getIsDeployEntityById(processDefinitionId.split(":")[0]);
        if (processDefinition == null) {
            throw new ServiceException(ApiError.PROCESS_DEFINITION_NOT_EXIST);
        }

        List<String> candidateUsers = getCandidateUsers(task, execution, propertiesDTO);
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(candidateUsers);
        if (CollectionUtils.isEmpty(userList)) {
            throw new ServiceException(ApiError.USER_NOT_EXIST_PARAM, JSONUtil.toJsonStr(candidateUsers));
        }

        Map<String, FindUserDTO> userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, e -> e));
        saveTaskManagementEntities(task, processInstanceId, activityId, processStartTime, propertiesDTO, executionId, activityName, candidateUsers, userMap,execution.getVariables());
    }

    /**
     * 获取当前节点名称
     * @param processInstance
     * @param task
     * @return
     */
    private String getActivityName(DelegateExecution processInstance, DelegateTask task) {
        return CharSequenceUtil.isNotBlank(processInstance.getCurrentActivityName()) ? processInstance.getCurrentActivityName() : task.getName();
    }

    /**
     * 获取当前节点ID
     * @param processInstance
     * @param task
     * @return
     */
    private String getActivityId(DelegateExecution processInstance, DelegateTask task) {
        return CharSequenceUtil.isNotBlank(processInstance.getCurrentActivityId()) ? processInstance.getCurrentActivityId() : task.getTaskDefinitionKey();
    }

    /**
     * 获取审批人
     * @param task 任务
     * @param execution 执行
     * @param propertiesDTO 扩展属性
     * @return 审批人
     */
    private List<String> getCandidateUsers(DelegateTask task, DelegateExecution execution, CamundaDTO.PropertiesDTO propertiesDTO) {
        List<String> candidateUsers = task.getCandidates().stream().map(IdentityLink::getUserId).collect(Collectors.toList());
        String assignee = task.getAssignee();
        if(CharSequenceUtil.isNotBlank(assignee)){
            candidateUsers= Arrays.asList(assignee.split(","));
        }
        if (CollectionUtils.isEmpty(candidateUsers)) {
            String startUserId = "" + execution.getVariable("creator");
            candidateUsers = addApproveInfo(startUserId, propertiesDTO, execution.getVariables());
        }
//        if ("somebody_exp".equals(propertiesDTO.getAssigneeOption()) && CharSequenceUtil.isNotBlank(propertiesDTO.getSomebody_exp())){
//            candidateUsers = replaceApproveVariables(Arrays.asList(propertiesDTO.getSomebody_exp().split(",")), execution.getVariables());
//        }

        return candidateUsers;
    }

    /**
     * 保存流程任务数据
     * @param task 任务
     * @param processInstanceId 流程实例ID
     * @param activityId 节点ID
     * @param processStartTime  流程开始时间
     * @param propertiesDTO 扩展属性
     * @param executionId 执行ID
     * @param activityName 节点名称
     * @param candidateUsers 审批人
     * @param userMap 审批人信息
     */
    private void saveTaskManagementEntities(DelegateTask task, String processInstanceId, String activityId, LocalDateTime processStartTime, CamundaDTO.PropertiesDTO propertiesDTO, String executionId, String activityName, List<String> candidateUsers, Map<String, FindUserDTO> userMap,Map<String, Object> variables) {
        List<FindUserDTO> copyUserList = getCopyUserList(propertiesDTO);
        candidateUsers.forEach(userId -> {
            FindUserDTO findUserDTO = userMap.get(userId);
            if (null == findUserDTO) {
                log.error("###ProcessManagementServiceImpl>>>saveTaskManagementEntities:::审批人不存在, userId: {}", userId);
                return;
            }
            //是否委托标识
            JSONObject labelJson = new JSONObject();
            labelJson.set("isDelegate",isDelegate(userId,variables));

            ProcessTaskManagementEntity insertTask = new ProcessTaskManagementEntity(processInstanceId, activityId, task.getId(), processStartTime, ApproveStatusEnum.APPROVE_ING, propertiesDTO, findUserDTO, executionId, activityName,labelJson);
            ProcessTaskManagementEntity taskManagementEntity = processTaskManagementService.saveProcessTask(insertTask);
            if (CollUtil.isNotEmpty(copyUserList)) {
                processTaskCcService.saveCcUser(task.getId(), copyUserList, taskManagementEntity.getId());
            }
        });
    }

    /**
     * 是否委托标识
     * @author will
     * @date 2025/6/3 19:28
     * @param variables
     * @return Boolean
     */
    private Boolean isDelegate (String userId,Map<String, Object> variables) {
        Boolean isDelegate = Boolean.FALSE;
        // 获取当前登录用户
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<Map<String, String>> delegateInfoList = (List<Map<String, String>>) variables.get("DELEGATE_INFO_"+ userInfo.getUid());
        if (CollUtil.isEmpty(delegateInfoList)) {
            return isDelegate;
        }
        for (Map<String, String> delegateInfo : delegateInfoList) {
            String delegateUser = delegateInfo.get("delegateUser");
            String delegateType = delegateInfo.get("delegateType");
            if (CamundaGlobalListener.AUTO_DELEGATE.equals(delegateType) && CharSequenceUtil.equals(userId,delegateUser)) {
                isDelegate = Boolean.TRUE;
            }
        }
        return isDelegate;
    }

    /**
     * 获取抄送人信息
     * @author will
     * @date 2025/5/20 16:01
     * @param propertiesDTO
     * @return List<FindUserDTO>
     */
    private List<FindUserDTO> getCopyUserList(CamundaDTO.PropertiesDTO propertiesDTO) {
       if (ProcessCopyOptionEnum.ROLE.getCode().equals(propertiesDTO.getCopyOption())) {
           List<String> ccRoleIds = Arrays.asList(propertiesDTO.getCopyRole().split(","));
           return sysUserFeign.getUserListByRoleIds(new SysFeignDTO.ListByRoleIdsDTO(ccRoleIds,""));
       } else if (ProcessCopyOptionEnum.USER.getCode().equals(propertiesDTO.getCopyOption())) {
           List<String> ccUserIds = Arrays.asList(propertiesDTO.getCopyUser().split(","));
           return sysUserFeign.getUserListByUserIds(ccUserIds);
       }
        return Collections.emptyList();
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
        noticeMsgInfoDTO.setContent(CharSequenceUtil.format("**单据名称: **{}\n**审批开始时间：**{} \n您有一笔审批即将超时，请尽快处理！", task.getBusinessName(), task.getStartTime()));
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
        if(CollectionUtils.isEmpty(taskList)){
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
            if(CollectionUtils.isEmpty(superiorList)){
                log.error("当前审批人无上级，无法转上级处理，审批人：{} ", task.getCurApproveId());
                return;
            }
            Map<String, List<UserSuperiorDTO>> superiorMap = superiorList.stream()
                    .filter(item -> ChargeSuperiorEnum.DIRECT_DEPARTMENT_CHARGE.getCode().equals(item.getLevel()))
                    .collect(Collectors.groupingBy(UserSuperiorDTO::getUserId));
            // 多个任务对应同一个上级
            for (String superiorId : superiorMap.keySet()) {
                List<UserSuperiorDTO> userSuperiors = superiorMap.get(superiorId);
                if(CollectionUtils.isEmpty(userSuperiors)){
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
            processManagementService.updateApprove(task.getTaskManagementId(), ApproveTypeEnum.REJECT, task.getManagementId(), task.getProcessInstanceId(), "审批超时，自动驳回", null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean transferBatch(ProcessManagementDTO.TransferBatchDTO dto) {
        // 查询当前执行任务
        List<ProcessManagementDTO.ManagementTaskDTO> managementTasks  = listTaskById(dto.getIds());
        if(CollectionUtils.isEmpty(managementTasks)){
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

            Map<String, Object> variables = runtimeService.getVariables(managementTask.getProcessInstanceId());
            //操作人
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            // 完成新增数据事务提交之后,发送MQ消息
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    //判断该单据类型是否有ERP审批同步定义
                    CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
                    mqDto.setProcessManagementId(managementTask.getManagementId());
                    mqDto.setBusinessName(managementTask.getBusinessName());
                    mqDto.setInstanceId(managementTask.getProcessInstanceId());
                    mqDto.setBusinessCode(managementTask.getBusinessCode());
                    mqDto.setCurTaskId(managementTask.getTaskId());
                    mqDto.setOperator(userInfo.getUid());
                    mqDto.setVariablesMap(variables);
                    mqDto.setBusinessKey(managementTask.getBusinessKey());
                    mqDto.setApproveType(FsActionStatusEnum.FORWARDED.getCode());//转办
                    syncFsExternalInstance(mqDto);
                }
            });
        }
        return Boolean.TRUE;
    }


    @Override
    public List<ProcessManagementDTO.ManagementTaskDTO> listTaskById(List<String> ids) {
        return baseMapper.listProcessTaskByIds(ids);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 180000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean endExecutionHandle(String processInstanceId) {
        // 根据流程实例id查询流程信息
        ProcessManagementEntity entity = lambdaQuery()
                .eq(ProcessManagementEntity::getProcessInstanceId, processInstanceId)
                .oneOpt().orElseThrow(() -> new ServiceException(ApiError.ERROR_PROCESS_NOT_EXIST));
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

        EndProcessDTO dto;
        if (ProcessManagementOptionEnum.PASS.getCode().equals(entity.getOption())) {
            //强制通过
            dto =  getPassOrRejectEndProcessDTO(variables,entity,ApproveTypeEnum.PASS,ProcessManagementOptionEnum.PASS);
        } else if (ProcessManagementOptionEnum.REJECT.getCode().equals(entity.getOption())) {
            //强制驳回、状态更新致待提交
            dto =  getPassOrRejectEndProcessDTO(variables,entity,ApproveTypeEnum.CANCEL,ProcessManagementOptionEnum.REJECT);
        } else {
            dto = getDefaultEndProcessDTO(variables,entity);
        }
        // 获取业务系统feign
        return callFeign(entity.getBusinessKey(), dto);
    }

    /**
     * 强制通过或驳回数据
     * @author will
     * @date 2025/5/22 15:33
     * @param variables
     * @param entity
     * @param typeEnum
     * @param optionEnum
     * @return EndProcessDTO
     */
    private EndProcessDTO getPassOrRejectEndProcessDTO (Map<String, Object> variables,ProcessManagementEntity entity,ApproveTypeEnum typeEnum, ProcessManagementOptionEnum optionEnum) {
        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        String deliveryDateStr = (String) variables.getOrDefault(DELIVERY_DATE, "");
        LocalDate deliveryDate = CharSequenceUtil.isNotBlank(deliveryDateStr) ? LocalDate.parse(deliveryDateStr) : LocalDate.now();
        // 流程信息传递给业务系统
        return new EndProcessDTO(entity, typeEnum.getStatus(),LocalDateTime.now(),userInfo.getUid(),optionEnum.getName(), deliveryDate,variables);
    }
    /**
     * 默认审核结束数据
     * @author will
     * @date 2025/5/22 15:32
     * @param variables
     * @param entity
     * @return EndProcessDTO
     */
    private EndProcessDTO getDefaultEndProcessDTO (Map<String, Object> variables,ProcessManagementEntity entity) {
        String lastApproveType = (String)variables.getOrDefault(LAST_APPROVE_TYPE, "");
        LocalDateTime lastApproveTime = (LocalDateTime) variables.get(LAST_APPROVE_TIME);
        String lastComment = (String) variables.getOrDefault(LAST_COMMENT, "");
        String lastApprover = (String) variables.getOrDefault(LAST_APPROVER, "");
        String deliveryDateStr = (String) variables.getOrDefault(DELIVERY_DATE, "");
        LocalDate deliveryDate = CharSequenceUtil.isNotBlank(deliveryDateStr) ? LocalDate.parse(deliveryDateStr) : LocalDate.now();
        // 流程信息传递给业务系统
        return new EndProcessDTO(entity, lastApproveType,lastApproveTime,lastApprover,lastComment, deliveryDate,variables);
    }
    /**
     * 回调更新状态
     * @author will
     * @date 2025/5/22 15:33
     * @param businessKey
     * @param dto
     * @return Boolean
     */
    @Override
    public Boolean callFeign(String businessKey, EndProcessDTO dto) {
        WorkMenuEntity menuEntity = workMenuService.getByModuleCode(businessKey);
        String feignBeanName = menuEntity.getFeignBeanName();
        if (CharSequenceUtil.isBlank(feignBeanName)) {
            throw new ServiceException(ApiError.ERROR_WORK_MENU_FEIGN);
        }
        BaseWorkflowService feignService = SpringUtil.getBean(feignBeanName);
        return feignService.approveEnd(dto);
    }

    @Override
    public Boolean disApproveFeign(ApproveDTO.DisApproveDTO dto) {
        WorkMenuEntity menuEntity = workMenuService.getByModuleCode(dto.getBusinessKey());
        String feignBeanName = menuEntity.getFeignBeanName();
        if (CharSequenceUtil.isBlank(feignBeanName)) {
            throw new ServiceException(ApiError.ERROR_WORK_MENU_FEIGN);
        }
        BaseWorkflowService feignService = SpringUtil.getBean(feignBeanName);
        return feignService.disApprove(dto);
    }

    @Override
    public Boolean cancelProcessFeign(ApproveDTO.CancelProcessDTO dto) {
        WorkMenuEntity menuEntity = workMenuService.getByModuleCode(dto.getBusinessKey());
        String feignBeanName = menuEntity.getFeignBeanName();
        if (CharSequenceUtil.isBlank(feignBeanName)) {
            throw new ServiceException(ApiError.ERROR_WORK_MENU_FEIGN);
        }
        BaseWorkflowService feignService = SpringUtil.getBean(feignBeanName);
        return feignService.cancelProcess(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProcessManagementDTO.StartResultDTO> batchStartProcess(ValidList<ProcessManagementDTO.StartDTO> dtoList) {
        List<ProcessManagementDTO.StartResultDTO> resultList = new ArrayList<>();
        dtoList.forEach(dto -> {
            // 启动流程
            ProcessManagementDTO.StartResultDTO resultDTO = processManagementService.startProcessManagement(dto);
            resultList.add(resultDTO);
        });
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProcessManagementDTO.ApproveResultDTO> batchApproveProcess(ValidList<ProcessManagementDTO.ApproveDTO> dtoList) {
        List<ProcessManagementDTO.ApproveResultDTO> resultList = new ArrayList<>();
        dtoList.forEach(approveDTO -> {
            // 审批流程
            ProcessManagementDTO.ApproveResultDTO resultDTO = processManagementService.approveProcess(approveDTO, Boolean.TRUE);
            resultList.add(resultDTO);
        });
        return resultList;
    }

    @Override
    public List<ProcessManagementDTO.CurApproveInfoDTO> batchCurApprover(ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList) {
        if(CollectionUtils.isEmpty(dtoList)){
           return Collections.emptyList();
        }
        Map<String, ProcessManagementDTO.HistoryActivityDTO> paramMap = dtoList
                .stream()
                .distinct()
                .collect(Collectors.toMap(k -> CharSequenceUtil.format("{}_{}", k.getBusinessId(), k.getBusinessKey()), e -> e));
        // 查询当前任务
        List<ProcessManagementDTO.CurApproveInfoDTO> resultList = baseMapper.listApproverByBusiness(dtoList);
        //处理数据
        handleCurApproveInfo(resultList);

        paramMap.keySet().forEach(paramKey -> {
            List<ProcessManagementDTO.CurApproveInfoDTO> curApproveList = resultList.stream()
                    .filter(item -> paramKey.equals(CharSequenceUtil.format("{}_{}", item.getBusinessId(), item.getBusinessKey())))
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(curApproveList)){
                resultList.add(new ProcessManagementDTO.CurApproveInfoDTO(paramMap.get(paramKey)));
            }
        });
        return resultList;
    }


    @Override
    public List<ProcessManagementDTO.CurApproveInfoDTO> batchCurApproverByApprove(ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList) {
        if(CollectionUtils.isEmpty(dtoList)){
            return Collections.emptyList();
        }
        // 查询当前任务
        List<ProcessManagementDTO.CurApproveInfoDTO> resultList = baseMapper.listApproverByApprover(dtoList);
        //处理数据
        handleCurApproveInfo(resultList);
        return resultList;
    }

    @Override
    public void completeTaskHandle(DelegateTask taskDelegate) {
        log.debug("completeTaskHandle finish ");
    }

    /**
     * @description: 根据业务key和业务id查询
     * @author Will
     * @date: 2023/7/4 15:47
     * @param businessKey
     * @param businessId
     * @return List<ProcessManagementEntity>
     */
    public ProcessManagementEntity getByBusiness(String businessKey, String businessId) {
        return lambdaQuery().eq(ProcessManagementEntity::getBusinessKey, businessKey)
                .eq(ProcessManagementEntity::getBusinessId, businessId)
                .eq(ProcessManagementEntity::getProcessStatus, ProcessStatusEnum.RUNNING)
                .last(SqlConstants.LIMIT_1)
                .one();
    }
    /**
     * 根据流程实例ID查询
     * @Author Luo_WG
     * @Date 2023/7/4 19:37
     * @param processInstanceId
     * @return com.erp.model.workflow.entity.ProcessManagementEntity
     **/
    @Override
    public ProcessManagementEntity getByProcessInstanceId(String processInstanceId) {
        return lambdaQuery().eq(ProcessManagementEntity::getProcessInstanceId, processInstanceId).last(SqlConstants.LIMIT_1).one();
    }

    @Override
    public void sameApproverAutoPass(ProcessManagementDTO.ApproveDTO dto, String processDefinitionId, String processInstanceId) {
        // 审批不通过不处理
        if(ApproveTypeEnum.REJECT.equals(dto.getApproveType())) {
            return;
        }
        ProcessDefinitionEntity processDefinition = processDefinitionService.getIsDeployEntityById(processDefinitionId);
        DictBasicEnum reviewSetting = processDefinition.getReviewSetting();
        // 审核节点不去重不处理
        if(DictBasicEnum.NO_DEDUPE.equals(reviewSetting)) {
            return;
        }
        List<String> exitTaskIdList = new ArrayList<>();
        Set<String> curTaskIdList;
        while (true){
            //查询当前待审批任务列表
            List<ProcessManagementDTO.ManagementTaskDTO> managementTaskList = getCurApproveTaskList(dto.getBusinessId(), dto.getBusinessKey(), "");
            if(CollectionUtils.isEmpty(managementTaskList)) {
                break;
            }
            Map<String, ProcessManagementDTO.ManagementTaskDTO> curTaskMap = managementTaskList.stream().collect(Collectors.toMap(ProcessManagementDTO.ManagementTaskDTO::getTaskManagementId, e -> e));
            curTaskIdList = curTaskMap.keySet();
            List<String> curTask = curTaskIdList.stream().filter(item -> !exitTaskIdList.contains(item))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(curTask)){
                break;
            }
            // 遍历待执行任务列表
            for (String taskId : curTask) {
                ProcessManagementDTO.ManagementTaskDTO managementTaskDTO = curTaskMap.get(taskId);
                sameApproveHandler(dto, managementTaskDTO, reviewSetting);
            }
            exitTaskIdList.addAll(curTaskIdList);
        }
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO processPass(String id) {
        ProcessManagementEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PROCESS_MANAGEMENT_NOT_EXIST);
        }
        if (!ProcessStatusEnum.PAUSE.equals(entity.getProcessStatus()) && !ProcessStatusEnum.RUNNING.equals(entity.getProcessStatus())) {
            throw new ServiceException(ApiError.PROCESS_MANAGEMENT_PASS_ERROR);
        }

        Map<String, Object> variables = runtimeService.getVariables(entity.getProcessInstanceId());

        entity.setProcessStatus(ProcessStatusEnum.FINISH);
        entity.setOption(ProcessManagementOptionEnum.PASS.getCode());
        entity.setApproveStatus(ApproveStatusEnum.APPROVE);
        entity.setEndTime(LocalDateTime.now());
        processManagementService.updateById(entity);

        //更新节点下任务状态为已审核
        processTaskManagementService.updateTaskStatus(entity.getProcessInstanceId(), ApproveStatusEnum.APPROVE);

        //强制通过终止流程
        try {
            runtimeService.deleteProcessInstance(entity.getProcessInstanceId(), "强制通过终止");
        } catch (ProcessEngineException e) {
            throw new ServiceException("强制驳回失败: " + e.getMessage(), e);
        }
        //操作人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //判断该单据类型是否有ERP审批同步定义
                CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
                mqDto.setProcessManagementId(entity.getId());
                mqDto.setBusinessName(entity.getBusinessName());
                mqDto.setBusinessCode(entity.getBusinessCode());
                mqDto.setInstanceId(entity.getProcessInstanceId());
                mqDto.setOperator(userInfo.getUid());
                mqDto.setVariablesMap(variables);
                mqDto.setBusinessKey(entity.getBusinessKey());
                mqDto.setApproveType(FsActionStatusEnum.PROCESSED.getCode());
                syncFsExternalInstance(mqDto);
            }
        });
        return BatchResultDTO.success(entity.getId(), entity.getBusinessCode(), OperationTypeEnum.PASS);
    }

    /**
     * 更新业务单据状态
     * @author will
     * @date 2025/5/22 11:06
     * @param entity
     * @param typeEnum
     * @param comment
     * @return Boolean
     */
    @Override
    public Boolean updateBusinessStatus(ProcessManagementEntity entity,ApproveTypeEnum typeEnum,String comment) {
        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        Map<String, Object> variables = runtimeService.getVariables(entity.getProcessInstanceId());
        String deliveryDateStr = (String) variables.getOrDefault(DELIVERY_DATE, "");
        LocalDate deliveryDate = CharSequenceUtil.isNotBlank(deliveryDateStr) ? LocalDate.parse(deliveryDateStr) : LocalDate.now();
        // 流程信息传递给业务系统
        EndProcessDTO dto = new EndProcessDTO(entity, typeEnum.getStatus(),LocalDateTime.now(),userInfo.getUid(),comment, deliveryDate,variables);
        // 获取业务系统feign
        return callFeign(entity.getBusinessKey(), dto);

    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO processReject(String id) {
        ProcessManagementEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PROCESS_MANAGEMENT_NOT_EXIST);
        }
        if (!ProcessStatusEnum.PAUSE.equals(entity.getProcessStatus()) && !ProcessStatusEnum.RUNNING.equals(entity.getProcessStatus())) {
            throw new ServiceException(ApiError.PROCESS_MANAGEMENT_REJECT_ERROR);
        }
        Map<String, Object> variables = runtimeService.getVariables(entity.getProcessInstanceId());

        entity.setProcessStatus(ProcessStatusEnum.TERMINATION);
        entity.setOption(ProcessManagementOptionEnum.REJECT.getCode());
        entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        entity.setEndTime(LocalDateTime.now());
        processManagementService.updateById(entity);

        //更新节点下任务状态为审核不通过
        processTaskManagementService.updateTaskStatus(entity.getProcessInstanceId(), ApproveStatusEnum.REJECT);

        //强制驳回终止流程
        try {
            runtimeService.deleteProcessInstance(entity.getProcessInstanceId(), "强制驳回终止");
        } catch (ProcessEngineException e) {
            throw new ServiceException("强制驳回失败: " + e.getMessage(), e);
        }


        //操作人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //判断该单据类型是否有ERP审批同步定义
                CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
                mqDto.setProcessManagementId(entity.getId());
                mqDto.setBusinessName(entity.getBusinessName());
                mqDto.setInstanceId(entity.getProcessInstanceId());
                mqDto.setBusinessCode(entity.getBusinessCode());
                mqDto.setOperator(userInfo.getUid());
                mqDto.setVariablesMap(variables);
                mqDto.setBusinessKey(entity.getBusinessKey());
                mqDto.setApproveType(FsActionStatusEnum.ROLLBACK.getCode());
                syncFsExternalInstance(mqDto);
            }
        });
        return BatchResultDTO.success(entity.getId(), entity.getBusinessCode(), OperationTypeEnum.REJECT);
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public BatchResultDTO processRestore(String id) {
        ProcessManagementEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PROCESS_MANAGEMENT_NOT_EXIST);
        }
        if (!ProcessStatusEnum.PAUSE.equals(entity.getProcessStatus())) {
            throw new ServiceException(ApiError.PROCESS_MANAGEMENT_RESTORE_ERROR);
        }
        Map<String, Object> variables = runtimeService.getVariables(entity.getProcessInstanceId());

        entity.setProcessStatus(ProcessStatusEnum.RUNNING);
        entity.setOption(ProcessManagementOptionEnum.RESTORE.getCode());
        processManagementService.updateById(entity);

        // 检查流程实例是否处于暂停状态
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(entity.getProcessInstanceId())
                .singleResult();
        if (instance == null) {
            throw new IllegalArgumentException("流程实例不存在");
        }
        if (!instance.isSuspended()) {
            throw new IllegalStateException("流程实例未处于暂停状态");
        }
        //恢复
        runtimeService.activateProcessInstanceById(entity.getProcessInstanceId());
        //操作人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //判断该单据类型是否有ERP审批同步定义
                CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
                mqDto.setProcessManagementId(entity.getId());
                mqDto.setBusinessName(entity.getBusinessName());
                mqDto.setInstanceId(entity.getProcessInstanceId());
                mqDto.setBusinessCode(entity.getBusinessCode());
                mqDto.setOperator(userInfo.getUid());
                mqDto.setVariablesMap(variables);
                mqDto.setBusinessKey(entity.getBusinessKey());
                mqDto.setApproveType(FsActionStatusEnum.RESTORE.getCode());
                syncFsExternalInstance(mqDto);
            }
        });
        return BatchResultDTO.success(entity.getId(), entity.getBusinessCode(), OperationTypeEnum.RESTORE);
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public BatchResultDTO processSuspend(String id) {
        ProcessManagementEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PROCESS_MANAGEMENT_NOT_EXIST);
        }
        if (!ProcessStatusEnum.RUNNING.equals(entity.getProcessStatus())) {
            throw new ServiceException(ApiError.PROCESS_MANAGEMENT_SUSPEND_ERROR);
        }
        Map<String, Object> variables = runtimeService.getVariables(entity.getProcessInstanceId());

        entity.setProcessStatus(ProcessStatusEnum.PAUSE);
        entity.setOption(ProcessManagementOptionEnum.PAUSE.getCode());
        processManagementService.updateById(entity);
        //暂停
        runtimeService.suspendProcessInstanceById(entity.getProcessInstanceId());
        //操作人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        // 完成新增数据事务提交之后,发送MQ消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //判断该单据类型是否有ERP审批同步定义
                CfgApproveSyncDTO.SyncFsProcessToMqDTO mqDto = new CfgApproveSyncDTO.SyncFsProcessToMqDTO();
                mqDto.setProcessManagementId(entity.getId());
                mqDto.setBusinessName(entity.getBusinessName());
                mqDto.setInstanceId(entity.getProcessInstanceId());
                mqDto.setBusinessCode(entity.getBusinessCode());
                mqDto.setOperator(userInfo.getUid());
                mqDto.setVariablesMap(variables);
                mqDto.setBusinessKey(entity.getBusinessKey());
                mqDto.setApproveType(FsActionStatusEnum.SUSPEND.getCode());
                syncFsExternalInstance(mqDto);
            }
        });
        return BatchResultDTO.success(entity.getId(), entity.getBusinessCode(), OperationTypeEnum.SUSPEND);
    }

    @Override
    public List<ProcessManagementDTO.TabListDTO> tabList(PermissionsDTO param) {
        List<ProcessManagementDTO.TabListDTO> tabList = this.baseMapper.tabList(param);
        Map<String, Integer> map = CollUtil.isEmpty(tabList) ? new HashMap<>() : tabList.stream().collect(Collectors.toMap(ProcessManagementDTO.TabListDTO::getTabFlag, ProcessManagementDTO.TabListDTO::getCount));
        ProcessManagementTabEnum[] values = ProcessManagementTabEnum.values();
        List<ProcessManagementDTO.TabListDTO> list = new ArrayList<>();
        for (ProcessManagementTabEnum item : values) {
            ProcessManagementDTO.TabListDTO resultDTO = new ProcessManagementDTO.TabListDTO();
            Integer count = map.get(item.getCode());
            //异常枚举额外处理
            if (ProcessManagementTabEnum.ABNORMAL.getCode().equals(item.getCode())) {
                count =  MathUtil.add(map.get(ProcessStatusEnum.PAUSE.getCode()),map.get(ProcessStatusEnum.TERMINATION.getCode()));
            }
            resultDTO.setCount(ObjectUtil.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public List<ProcessManagementDTO.DetailPagingResultDTO> listDetail(ProcessManagementDTO.DetailSearchDTO dto) {
        if (ProcessSourcePlatformEnum.ERP.getCode().equals(dto.getSourcePlatform())) {
            return baseMapper.listErpDetail(dto);
        }
        List<ProcessManagementDTO.DetailPagingResultDTO> detailPagingResultList = baseMapper.listFsDetail(dto);
        handleFsDetailPaging(detailPagingResultList);
        return detailPagingResultList;
    }

    /**
     * 飞书数据处理
     * @author will
     * @date 2025/6/4 14:57
     * @param detailPagingResultList
     * @return void
     */
    private void handleFsDetailPaging(List<ProcessManagementDTO.DetailPagingResultDTO> detailPagingResultList) {
        if (CollUtil.isEmpty(detailPagingResultList)) {
            return;
        }
        List<String> userIdList = detailPagingResultList.stream().map(ProcessManagementDTO.DetailPagingResultDTO::getCurApproveId).distinct().collect(Collectors.toList());
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        if (CollUtil.isEmpty(userList)) {
            return;
        }
        Map<String, String> map = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));
        for (ProcessManagementDTO.DetailPagingResultDTO resultDTO : detailPagingResultList) {
            resultDTO.setCurApproveName(map.get(resultDTO.getCurApproveId()));
        }
    }

    @Override
    public PagingVO<ProcessManagementDTO.MainPagingResultDTO> mainPaging(PagingDTO<ProcessManagementDTO.SearchDTO> pageDTO) {
        pageDTO.getParams().setPermissionSql(pageDTO.getPermissionSql());
        // 查询流程实例
        Page<ProcessManagementDTO.MainPagingResultDTO> query = new Page<>(pageDTO.getCurrPage(), pageDTO.getPageSize());
        IPage<ProcessManagementDTO.MainPagingResultDTO> pageData = baseMapper.mainPaging(query, pageDTO.getParams());
        pageData.getRecords().forEach(record -> {
            if (record.getProcessStatus() != null) {
                record.setProcessStatusName(record.getProcessStatus().getName());
            }
            if (record.getSourcePlatform() != null) {
                record.setSourcePlatformName(ProcessSourcePlatformEnum.getName(record.getSourcePlatform()));
            }
            record.setBusinessKeyName(SourceTypeEnum.getName(record.getBusinessKey()));
        });
        return new PagingVO<>(pageData);
    }

    /**
     * @description: 根据业务key和业务id查询
     * @author Will
     * @date: 2023/7/4 15:47
     * @param businessKey
     * @param businessId
     * @return List<ProcessManagementEntity>
     */
    private List<ProcessManagementEntity> listByBusiness(String businessKey,String businessId) {
        return lambdaQuery()
                .eq(ProcessManagementEntity::getBusinessKey, businessKey)
                .eq(ProcessManagementEntity::getBusinessId, businessId)
                // 暂停或进行中
                .in(ProcessManagementEntity::getProcessStatus, Arrays.asList(ProcessStatusEnum.PAUSE, ProcessStatusEnum.RUNNING))
                .last(SqlConstants.LIMIT_1)
                .list();
    }

    /**
     * 将审批人变量替换为流程变量中的实际值。
     *
     * @param approves 审批人列表（可以包含格式为 #{variableName} 的变量）
     * @param variables 流程变量
     * @return 实际审批人列表
     */
    public static List<String> replaceApproveVariables(List<String> approves, Map<String, Object> variables) {
        if (CollectionUtils.isEmpty(approves)) {
            return approves;
        }
        // 遍历approves，将其中的变量替换为实陫值
        return approves.stream().map(approve -> {
            String valueFromPath = JsonPathUtil.getValueFromPath(variables, approve);
            return CharSequenceUtil.isNotBlank(valueFromPath) ? valueFromPath : StringUtil.EMPTY_STRING;
        }).filter(StrUtil::isNotBlank).collect(Collectors.toList());
    }

    @Override
    public List<String> getTestList(String businessType) {
        return baseMapper.getTestList(businessType);
    }

    @Override
    public Boolean checkSubmitByBusinessId(ProcessManagementDTO.CheckSubmitByBusinessIdDTO dto) {
        //获取创建时间最新的一条数据
        ProcessManagementEntity processManagementEntity = lambdaQuery()
                .eq(ProcessManagementEntity::getBusinessKey, dto.getBusinessKey())
                .eq(ProcessManagementEntity::getBusinessId, dto.getBusinessId())
                .orderByDesc(ProcessManagementEntity::getCreateTime)
                .last(SqlConstants.LIMIT_1)
                .one();
        if(Objects.isNull(processManagementEntity)){
            return Boolean.FALSE;
        }
        List<ProcessTaskManagementEntity> list = processTaskManagementService.lambdaQuery().eq(ProcessTaskManagementEntity::getProcessInstanceId, processManagementEntity.getProcessInstanceId()).list();
        if(CollUtil.isEmpty(list)){
            return Boolean.FALSE;
        }
        return list.stream().map(ProcessTaskManagementEntity::getTaskStatus).allMatch(e -> e.equals(ApproveStatusEnum.APPROVE_ING));
    }

    @Override
    public Boolean checkTaskByProcessInstanceId(String processInstanceId) {
        ProcessManagementEntity processManagementEntity = lambdaQuery()
                .eq(ProcessManagementEntity::getProcessInstanceId, processInstanceId)
                .eq(ProcessManagementEntity::getProcessStatus, ProcessStatusEnum.RUNNING)
                .last("limit 1")
                .one();
        if(Objects.isNull(processManagementEntity)){
            return Boolean.FALSE;
        }

        List<ProcessTaskManagementEntity> list = processTaskManagementService.lambdaQuery().eq(ProcessTaskManagementEntity::getProcessInstanceId, processInstanceId).list();
        if(CollUtil.isEmpty(list)){
            return Boolean.FALSE;
        }
        List<String> taskIds = list.stream().map(ProcessTaskManagementEntity::getTaskId).distinct().collect(Collectors.toList());
        if(taskIds.size()  ==  1 ){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public List<ProcessManagementEntity> listDoing(List<String> processDefinitionIdList, List<Integer> processDefinitionVersionList) {
        if (CollUtil.isEmpty(processDefinitionIdList) || CollUtil.isEmpty(processDefinitionVersionList)) {
            return Collections.emptyList();
        }
       return lambdaQuery().in(ProcessManagementEntity::getProcessDefinitionId,processDefinitionIdList)
                .in(ProcessManagementEntity::getProcessVersion,processDefinitionVersionList)
                .in(ProcessManagementEntity::getProcessStatus, Arrays.asList(ProcessStatusEnum.PAUSE, ProcessStatusEnum.RUNNING))
                .list();
    }

    /**
     * 数据处理
     * @author will
     * @date 2025/7/17 12:08
     * @param resultList
     * @return void
     */
    private void handleCurApproveInfo (List<ProcessManagementDTO.CurApproveInfoDTO> resultList) {
        if (CollUtil.isEmpty(resultList)) {
            return;
        }
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String, String> userMap = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));

        for (ProcessManagementDTO.CurApproveInfoDTO curApproveInfoDTO : resultList) {
            //飞书流程业务名称为空需要处理
            curApproveInfoDTO.setBusinessName(CharSequenceUtil.isNotBlank(curApproveInfoDTO.getBusinessName()) ? curApproveInfoDTO.getBusinessName() : SourceTypeEnum.getName(curApproveInfoDTO.getBusinessKey()));
            //飞书流程人员名称为空需要处理
            if (CharSequenceUtil.isBlank(curApproveInfoDTO.getCurApproveName()) && CharSequenceUtil.isNotBlank(curApproveInfoDTO.getCurApproveId())) {
                String[] curApproveIds = curApproveInfoDTO.getCurApproveId().split(",");
                String curApproveName = Arrays.stream(curApproveIds).map(userMap::get).collect(Collectors.joining(","));
                curApproveInfoDTO.setCurApproveName(curApproveName);
            }
        }
    }
}
