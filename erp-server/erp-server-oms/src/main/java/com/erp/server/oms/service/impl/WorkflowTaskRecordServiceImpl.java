package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.annotation.DistributeLocker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.DistributeKeyConstant;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.WorkflowTaskNodeConfigDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.KolSubB2cApplicationEntity;
import com.erp.model.oms.entity.WorkflowTaskInstanceEntity;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.WorkflowTaskInstanceStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.WorkflowTaskRecordMapper;
import com.erp.server.oms.orchestration.WorkflowTaskNodeConfigParser;
import com.erp.server.oms.orchestration.WorkflowTaskStepDispatcher;
import com.erp.server.oms.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务节点记录表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-09-16
 */
@Slf4j
@Service
public class WorkflowTaskRecordServiceImpl extends SuperServiceImpl<WorkflowTaskRecordMapper, WorkflowTaskRecordEntity> implements WorkflowTaskRecordService {

    private static final String FORCE_RETRY_PERMISSION = "oms:workflowTaskRecord:forceRetry";

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private KolSubB2cApplicationService kolSubB2cApplicationService;

    @Lazy
    @Resource
    private WorkflowTaskInstanceService workflowTaskInstanceService;

    @Lazy
    @Resource
    private WorkflowTaskStepDispatcher workflowTaskStepDispatcher;

    /**
     * 添加工作流任务记录
     *
     * @param dto 添加任务的数据传输对象，包含字典类型、源ID和源类型等信息
     * @return 创建的工作流任务记录实体列表
     * @throws ServiceException 当指定类型的工作流任务节点字典不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkflowTaskRecordEntity> addTask(WorkflowTaskRecordDTO.AddTaskDTO dto) {
        List<WorkflowTaskRecordEntity> entities = buildTaskEntities(dto);
        try {
            saveBatch(entities);
            dto.setInstanceId(entities.get(0).getInstanceId());
            return entities;
        } catch (DuplicateKeyException e) {
            log.warn("任务节点已存在，sourceType={}, sourceId={}", dto.getSourceTypeEnum().getCode(), dto.getSourceId(), e);
            throw new ServiceException(ApiError.WF_TASK_RECORD_DUPLICATE);
        }
    }

    /**
     * 创建任务并在事务提交后调度首节点（index=0）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkflowTaskRecordEntity> addTaskAndStart(WorkflowTaskRecordDTO.AddTaskDTO dto) {
        List<WorkflowTaskRecordEntity> entities = addTask(dto);
        WorkflowTaskRecordDTO.AddTaskDTO dispatch = buildDispatchDto(
                workflowTaskInstanceService.getById(dto.getInstanceId()), dto);
        dispatch.setTargetIndex(0);
        dispatch.setRetryFailedStep(Boolean.FALSE);
        registerDispatchAfterCommit(dispatch, dto.getSourceId());
        return entities;
    }

    /**
     * 启动或恢复编排入口：参数校验后委托带锁实现，防止并发重复发 MQ。
     */
    @Override
    public void startOrResume(WorkflowTaskRecordDTO.AddTaskDTO dto) {
        if (dto == null || dto.getSourceTypeEnum() == null || CharSequenceUtil.isBlank(dto.getSourceId())) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_PARAM_INCOMPLETE);
        }
        SpringUtil.getBean(WorkflowTaskRecordServiceImpl.class).startOrResumeWithLock(dto);
    }

    /**
     * 带分布式锁的启动/恢复逻辑，按实例状态决定新建、跳过或调度当前节点。
     */
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.WORKFLOW_LOCK_KEY,
            keyName = "sourceTypeEnum.code,sourceId", unlockAfterTx = true)
    public void startOrResumeWithLock(WorkflowTaskRecordDTO.AddTaskDTO dto) {
        WorkflowTaskInstanceEntity latest = workflowTaskInstanceService.getLatestBySource(
                dto.getSourceId(), dto.getSourceTypeEnum().getCode());

        if (latest != null && isTerminalInstance(latest.getStatus())) {
            addTask(dto);
            WorkflowTaskRecordDTO.AddTaskDTO dispatch = buildDispatchDto(
                    workflowTaskInstanceService.getById(dto.getInstanceId()), dto);
            dispatch.setTargetIndex(0);
            dispatch.setRetryFailedStep(Boolean.FALSE);
            registerDispatchAfterCommit(dispatch, dto.getSourceId());
            return;
        }

        if (latest != null && isActiveInstance(latest.getStatus())) {
            if (trySkipDuplicateDispatch(latest)) {
                log.info("编排实例运行中且当前节点未超时，跳过重复调度，instanceId={}", latest.getId());
                return;
            }
            WorkflowTaskRecordEntity currentStep = resolveCurrentStep(latest);
            if (currentStep == null) {
                log.warn("编排实例无有效节点，instanceId={}", latest.getId());
                return;
            }
            prepareStaleProcessingStep(currentStep);
            WorkflowTaskRecordDTO.AddTaskDTO dispatch = buildDispatchDto(latest, dto);
            dispatch.setTargetIndex(currentStep.getIndex());
            dispatch.setRetryFailedStep(WorkflowTaskRecordStatusEnum.FAILED.getCode().equals(currentStep.getStatus()));
            registerDispatchAfterCommit(dispatch, latest.getSourceId());
            return;
        }

        List<WorkflowTaskRecordEntity> allExisting = listBySourceId(dto.getSourceId(), dto.getSourceTypeEnum().getCode());
        if (CollUtil.isEmpty(allExisting)) {
            addTask(dto);
        } else {
            String instanceIdOnSteps = allExisting.stream()
                    .map(WorkflowTaskRecordEntity::getInstanceId)
                    .filter(CharSequenceUtil::isNotBlank)
                    .findFirst()
                    .orElse(null);
            if (CharSequenceUtil.isNotBlank(instanceIdOnSteps)) {
                dto.setInstanceId(instanceIdOnSteps);
                if (latest == null) {
                    latest = workflowTaskInstanceService.getById(instanceIdOnSteps);
                }
            } else if (latest == null) {
                latest = workflowTaskInstanceService.ensureInstanceForLegacy(dto,
                        listLegacyBySourceId(dto.getSourceId(), dto.getSourceTypeEnum().getCode()));
            } else {
                dto.setInstanceId(latest.getId());
            }
        }
        WorkflowTaskInstanceEntity instance = CharSequenceUtil.isNotBlank(dto.getInstanceId())
                ? workflowTaskInstanceService.getById(dto.getInstanceId())
                : latest;
        WorkflowTaskRecordDTO.AddTaskDTO dispatch = buildDispatchDto(instance, dto);
        if (dispatch.getTargetIndex() == null) {
            dispatch.setTargetIndex(0);
        }
        dispatch.setRetryFailedStep(Boolean.FALSE);
        registerDispatchAfterCommit(dispatch, dto.getSourceId());
    }

    /**
     * 判断编排实例是否已进入终态（成功/已取消）。
     */
    private boolean isTerminalInstance(String status) {
        return WorkflowTaskInstanceStatusEnum.SUCCESS.getCode().equals(status)
                || WorkflowTaskInstanceStatusEnum.CANCELLED.getCode().equals(status);
    }

    /**
     * 判断编排实例是否处于可继续调度的活跃态。
     */
    private boolean isActiveInstance(String status) {
        return WorkflowTaskInstanceStatusEnum.RUNNING.getCode().equals(status)
                || WorkflowTaskInstanceStatusEnum.WAITING.getCode().equals(status)
                || WorkflowTaskInstanceStatusEnum.FAILED.getCode().equals(status);
    }

    /**
     * 运行中实例防重：当前节点仍在 PROCESSING 且未超时则跳过重复调度。
     */
    private boolean trySkipDuplicateDispatch(WorkflowTaskInstanceEntity instance) {
        WorkflowTaskRecordEntity current = resolveCurrentStep(instance);
        if (current == null) {
            return false;
        }
        if (!WorkflowTaskRecordStatusEnum.PROCESSING.getCode().equals(current.getStatus())) {
            return false;
        }
        LocalDateTime updateTime = current.getUpdateTime();
        return Objects.nonNull(updateTime)
                && updateTime.plusMinutes(TASK_PROCESSING_TIMEOUT_MINUTES).isAfter(LocalDateTime.now());
    }

    /**
     * 定位实例当前应执行节点：优先 currentIndex，找不到则取首个非成功节点。
     */
    private WorkflowTaskRecordEntity resolveCurrentStep(WorkflowTaskInstanceEntity instance) {
        if (CharSequenceUtil.isBlank(instance.getId())) {
            return null;
        }
        if (instance.getCurrentIndex() != null) {
            WorkflowTaskRecordEntity byIndex = this.lambdaQuery()
                    .eq(WorkflowTaskRecordEntity::getInstanceId, instance.getId())
                    .eq(WorkflowTaskRecordEntity::getIndex, instance.getCurrentIndex())
                    .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                    .one();
            if (byIndex != null) {
                return byIndex;
            }
        }
        return this.lambdaQuery()
                .eq(WorkflowTaskRecordEntity::getInstanceId, instance.getId())
                .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                .ne(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.SUCCESS.getCode())
                .orderByAsc(WorkflowTaskRecordEntity::getIndex)
                .last("limit 1")
                .one();
    }

    /**
     * 对超时卡住的 PROCESSING 节点做状态回拨，便于重新调度。
     */
    private void prepareStaleProcessingStep(WorkflowTaskRecordEntity currentStep) {
        if (!WorkflowTaskRecordStatusEnum.PROCESSING.getCode().equals(currentStep.getStatus())) {
            return;
        }
        if (Boolean.TRUE.equals(resetStaleProcessingTask(currentStep.getId()))) {
            currentStep.setStatus(WorkflowTaskRecordStatusEnum.PENDING.getCode());
        }
    }

    /**
     * 补齐字典中新增而库内缺失的节点，并继承上一成功节点的 output 作为 input。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkflowTaskRecordEntity> addMissingTask(WorkflowTaskRecordDTO.AddTaskDTO dto, List<WorkflowTaskRecordEntity> existTasks) {
        String instanceId = CollUtil.emptyIfNull(existTasks).stream()
                .map(WorkflowTaskRecordEntity::getInstanceId)
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst()
                .orElse(null);
        List<WorkflowTaskRecordEntity> mergedExistTasks;
        if (CharSequenceUtil.isNotBlank(instanceId)) {
            mergedExistTasks = this.lambdaQuery()
                    .eq(WorkflowTaskRecordEntity::getInstanceId, instanceId)
                    .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                    .orderByAsc(WorkflowTaskRecordEntity::getIndex)
                    .list();
        } else {
            mergedExistTasks = new ArrayList<>(listLegacyTasksBySource(dto.getSourceId(), dto.getSourceTypeEnum().getCode()));
            mergedExistTasks.addAll(CollUtil.emptyIfNull(existTasks));
        }
        Map<Integer, WorkflowTaskRecordEntity> existTaskMap = buildIndexTaskMap(mergedExistTasks);
        if (CollUtil.isEmpty(mergedExistTasks)) {
            return Collections.emptyList();
        }
        if (CharSequenceUtil.isBlank(instanceId)) {
            instanceId = mergedExistTasks.stream()
                    .map(WorkflowTaskRecordEntity::getInstanceId)
                    .filter(CharSequenceUtil::isNotBlank)
                    .findFirst()
                    .orElse(null);
        }
        if (CharSequenceUtil.isBlank(instanceId)) {
            log.warn("补齐节点失败，未找到 instanceId，sourceType={}, sourceId={}",
                    dto.getSourceTypeEnum().getCode(), dto.getSourceId());
            return Collections.emptyList();
        }
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByType(
                dto.getDictBasicTypeEnum().getType(), dto.getSourceTypeEnum().getCode());
        if (CollUtil.isEmpty(dictList)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, dto.getSourceTypeEnum().getName());
        }
        dictList = dictList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(DictBasicDTO.ViewDTO::getSort))
                .collect(Collectors.toList());
        List<WorkflowTaskRecordEntity> missingEntities = new ArrayList<>();
        for (int i = 0; i < dictList.size(); i++) {
            if (existTaskMap.containsKey(i)) {
                continue;
            }
            DictBasicDTO.ViewDTO viewDTO = dictList.get(i);
            WorkflowTaskRecordEntity missingEntity = new WorkflowTaskRecordEntity();
            missingEntity.setId(IdWorker.getIdStr());
            missingEntity.setInstanceId(instanceId);
            missingEntity.setSourceId(dto.getSourceId());
            missingEntity.setSourceType(dto.getSourceTypeEnum().getCode());
            missingEntity.setSourceCode(dto.getSourceCode());
            missingEntity.setClassPath(viewDTO.getValue());
            applyNodeTarget(missingEntity, viewDTO.getValue());
            missingEntity.setIndex(i);
            missingEntity.setDictBasicId(viewDTO.getId());
            missingEntity.setTraceId(dto.getTraceId());
            missingEntity.setStatus(WorkflowTaskRecordStatusEnum.PENDING.getCode());
            missingEntity.setRetryCount(0);
            String previousOutputData = getPreviousSuccessOutputDataInternal(missingEntity, existTaskMap);
            if (CharSequenceUtil.isNotBlank(previousOutputData)) {
                missingEntity.setInputData(previousOutputData);
            }
            missingEntities.add(missingEntity);
        }
        if (CollUtil.isEmpty(missingEntities)) {
            return Collections.emptyList();
        }
        try {
            saveBatch(missingEntities);
            return missingEntities;
        } catch (DuplicateKeyException e) {
            log.warn("任务节点补齐命中唯一约束，sourceType={}, sourceId={}", dto.getSourceTypeEnum().getCode(), dto.getSourceId(), e);
            throw new ServiceException(ApiError.WF_TASK_RECORD_DUPLICATE);
        }
    }

    /**
     * 按字典配置创建编排实例及全部节点实体（尚未落库）。
     */
    private List<WorkflowTaskRecordEntity> buildTaskEntities(WorkflowTaskRecordDTO.AddTaskDTO dto) {
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByType(dto.getDictBasicTypeEnum().getType(), dto.getSourceTypeEnum().getCode());
        if (CollUtil.isEmpty(dictList)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, dto.getSourceTypeEnum().getName());
        }
        dictList = dictList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(DictBasicDTO.ViewDTO::getSort))
                .collect(Collectors.toList());

        WorkflowTaskInstanceEntity instance = workflowTaskInstanceService.createInstance(dto, dictList.size());
        dto.setInstanceId(instance.getId());

        List<WorkflowTaskRecordEntity> entities = new ArrayList<>();
        for (int i = 0; i < dictList.size(); i++) {
            DictBasicDTO.ViewDTO viewDTO = dictList.get(i);
            WorkflowTaskRecordEntity entity = new WorkflowTaskRecordEntity();
            entity.setId(IdWorker.getIdStr());
            entity.setInstanceId(instance.getId());
            entity.setSourceId(dto.getSourceId());
            entity.setSourceType(dto.getSourceTypeEnum().getCode());
            entity.setSourceCode(dto.getSourceCode());
            entity.setClassPath(viewDTO.getValue());
            applyNodeTarget(entity, viewDTO.getValue());
            entity.setIndex(i);
            entity.setDictBasicId(viewDTO.getId());
            entity.setTraceId(dto.getTraceId());
            entity.setStatus(WorkflowTaskRecordStatusEnum.PENDING.getCode());
            entity.setRetryCount(0);
            if (i == 0) {
                entity.setInputData(JSON.toJSONString(dto.getFirstNodeInputData()));
            }
            entities.add(entity);
        }
        return entities;
    }

    /** 解析节点配置并写入 target_service、target_endpoint 冗余字段，供监控展示。 */
    private void applyNodeTarget(WorkflowTaskRecordEntity entity, String rawNodeConfig) {
        WorkflowTaskNodeConfigDTO config = WorkflowTaskNodeConfigParser.parse(rawNodeConfig);
        entity.setTargetService(CharSequenceUtil.blankToDefault(config.getServiceCode(), ""));
        entity.setTargetEndpoint(WorkflowTaskNodeConfigParser.buildTargetEndpoint(config));
    }


    /**
     * 查询异常节点列表（用于补偿任务扫描）。
     */
    @Override
    public List<WorkflowTaskRecordEntity> listErrorTask() {
        return baseMapper.listErrorTask("");
    }

    /**
     * 定时补偿入口：按实例维度聚合异常节点并触发最小 index 节点重试。
     */
    @Override
    public void WorkflowTaskRecordRetryJob(String id) {
        compensateStuckChainSteps();
        List<WorkflowTaskRecordEntity> list = baseMapper.listErrorTask(id);
        if (CollectionUtil.isEmpty(list)) {
            return ;
        }
        Map<String, List<WorkflowTaskRecordEntity>> map = list.stream()
                .collect(Collectors.groupingBy(this::resolveRetryJobGroupKey));
        for (Map.Entry<String, List<WorkflowTaskRecordEntity>> entry : map.entrySet()) {
            List<WorkflowTaskRecordEntity> instanceTasks = entry.getValue();
            if (CollUtil.isEmpty(instanceTasks)) {
                continue;
            }
            long count = instanceTasks.stream()
                    .filter(e -> Objects.equals(e.getStatus(), WorkflowTaskRecordStatusEnum.FAILED.getCode())
                            && Optional.ofNullable(e.getRetryCount()).orElse(0) > AUTO_RETRY_MAX_COUNT).count();
            if (count > 0) {
                continue;
            }
            WorkflowTaskRecordEntity entity = instanceTasks.stream()
                    .filter(e -> !WorkflowTaskRecordStatusEnum.SUCCESS.getCode().equals(e.getStatus()))
                    .min(Comparator.comparing(WorkflowTaskRecordEntity::getIndex))
                    .orElse(instanceTasks.get(0));
            WorkflowTaskInstanceEntity instance = resolveRetryJobInstance(entity, instanceTasks);
            if (instance == null) {
                log.warn("任务节点补偿重试跳过，未找到有效实例，sourceType={}, sourceId={}, instanceId={}",
                        entity.getSourceType(), entity.getSourceId(), entity.getInstanceId());
                continue;
            }
            if (WorkflowTaskInstanceStatusEnum.SUCCESS.getCode().equals(instance.getStatus())
                    || WorkflowTaskInstanceStatusEnum.CANCELLED.getCode().equals(instance.getStatus())) {
                continue;
            }
            WorkflowTaskRecordDTO.AddTaskDTO template = new WorkflowTaskRecordDTO.AddTaskDTO();
            template.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE);
            template.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.getByCode(entity.getSourceType()));
            if (Objects.isNull(template.getSourceTypeEnum())) {
                log.warn("任务节点记录补偿重试失败，未知sourceType={}", entity.getSourceType());
                continue;
            }
            template.setTraceId(entity.getTraceId());
            template.setTargetIndex(entity.getIndex());
            WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = buildDispatchDto(instance, template);
            addTaskDTO.setRetryFailedStep(Boolean.TRUE);
            try {
                workflowTaskStepDispatcher.sendDispatchMq(addTaskDTO, entity.getSourceId());
            } catch (Exception ex) {
                XxlJobHelper.log(StrUtil.format("任务节点补偿重试MQ异常，{}", ex.getMessage()));
            }
        }
    }

    /**
     * 计算补偿分组键：优先 instance 维度，否则回退 legacy 维度。
     */
    private String resolveRetryJobGroupKey(WorkflowTaskRecordEntity entity) {
        if (CharSequenceUtil.isNotBlank(entity.getInstanceId())) {
            return "instance:" + entity.getInstanceId();
        }
        return "legacy:" + entity.getSourceType() + ":" + entity.getSourceId();
    }

    /**
     * 解析补偿任务对应实例：优先节点上的 instanceId，其次按 source 查询最新实例，
     * 若仍不存在且为历史 legacy 节点，则在 Job 内补建实例并回填 instance_id。
     */
    private WorkflowTaskInstanceEntity resolveRetryJobInstance(WorkflowTaskRecordEntity entity,
                                                               List<WorkflowTaskRecordEntity> groupTasks) {
        if (CharSequenceUtil.isNotBlank(entity.getInstanceId())) {
            WorkflowTaskInstanceEntity instance = workflowTaskInstanceService.getById(entity.getInstanceId());
            if (instance != null && !Boolean.TRUE.equals(instance.getIsDeleted())) {
                return instance;
            }
            return null;
        }
        WorkflowTaskInstanceEntity latest = workflowTaskInstanceService.getLatestBySource(entity.getSourceId(), entity.getSourceType());
        if (latest != null) {
            return latest;
        }

        WorkflowTaskRecordTypeEnum sourceTypeEnum = WorkflowTaskRecordTypeEnum.getByCode(entity.getSourceType());
        if (Objects.isNull(sourceTypeEnum)) {
            log.warn("任务节点补偿重试跳过，legacy 节点 sourceType 无效，sourceType={}, sourceId={}",
                    entity.getSourceType(), entity.getSourceId());
            return null;
        }
        WorkflowTaskRecordDTO.AddTaskDTO dto = new WorkflowTaskRecordDTO.AddTaskDTO();
        dto.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE);
        dto.setSourceTypeEnum(sourceTypeEnum);
        dto.setSourceId(entity.getSourceId());
        dto.setSourceCode(entity.getSourceCode());
        dto.setTraceId(entity.getTraceId());

        List<WorkflowTaskRecordEntity> legacySteps = CollUtil.isNotEmpty(groupTasks)
                ? groupTasks
                : listLegacyTasksBySource(entity.getSourceId(), entity.getSourceType());
        if (CollUtil.isEmpty(legacySteps)) {
            return null;
        }
        WorkflowTaskInstanceEntity created = workflowTaskInstanceService.ensureInstanceForLegacy(dto, legacySteps);
        if (created != null) {
            log.info("任务节点补偿重试为历史数据补建实例成功，sourceType={}, sourceId={}, instanceId={}",
                    entity.getSourceType(), entity.getSourceId(), created.getId());
        }
        return created;
    }

    /**
     * 聚合查询节点异常报表数据。
     */
    @Override
    public List<WorkflowTaskRecordDTO.TaskErrorReportDTO> getTaskErrorReport() {
        return  baseMapper.getTaskErrorReport();
    }

    /**
     * 人工强制重试：解析 source 维度后委托带锁实现，保证同一单据串行重试。
     */
    @Override
    public WorkflowTaskRecordDTO.ForceRetryResultDTO forceRetry(WorkflowTaskRecordDTO.ForceRetryDTO dto) {
        if (Objects.isNull(dto)) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_PARAM_REQUIRED);
        }
        String sourceType;
        String sourceId;
        if (CharSequenceUtil.isNotBlank(dto.getId())) {
            WorkflowTaskRecordEntity lockTask = getById(dto.getId());
            if (Objects.isNull(lockTask)) {
                throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND);
            }
            sourceType = lockTask.getSourceType();
            sourceId = lockTask.getSourceId();
        } else {
            if (CharSequenceUtil.isBlank(dto.getSourceType()) || CharSequenceUtil.isBlank(dto.getSourceId())) {
                throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_PARAM_INCOMPLETE);
            }
            sourceType = dto.getSourceType();
            sourceId = dto.getSourceId();
        }
        return SpringUtil.getBean(WorkflowTaskRecordServiceImpl.class).forceRetryWithLock(dto, sourceType, sourceId);
    }

    /**
     * 带锁执行强制重试：校验权限、重置 eligible 节点、同步实例状态并发送单步 MQ。
     */
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.WORKFLOW_LOCK_KEY, keyName = "sourceType,sourceId", unlockAfterTx = true)
    public WorkflowTaskRecordDTO.ForceRetryResultDTO forceRetryWithLock(WorkflowTaskRecordDTO.ForceRetryDTO dto, String sourceType, String sourceId) {
        checkForceRetryPermission();
        List<WorkflowTaskRecordEntity> taskList = listForceRetryTasks(dto);
        if (CollUtil.isEmpty(taskList)) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND);
        }
        Map<String, List<WorkflowTaskRecordEntity>> taskGroup = taskList.stream()
                .collect(Collectors.groupingBy(this::resolveRetryJobGroupKey));
        int resetCount = 0;
        int mqCount = 0;
        boolean retryByNodeId = CharSequenceUtil.isNotBlank(dto.getId());
        for (List<WorkflowTaskRecordEntity> groupTasks : taskGroup.values()) {
            List<WorkflowTaskRecordEntity> resetTasks = groupTasks.stream()
                    .filter(e -> !retryByNodeId || Objects.equals(e.getId(), dto.getId()))
                    .filter(this::allowForceRetry)
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(resetTasks)) {
                continue;
            }
            Map<Integer, WorkflowTaskRecordEntity> indexTaskMap = buildIndexTaskMap(groupTasks);
            for (WorkflowTaskRecordEntity entity : resetTasks) {
                resetForceRetryTask(entity, dto, indexTaskMap);
                resetCount++;
            }
            WorkflowTaskRecordEntity dispatchTask = resetTasks.stream()
                    .min(Comparator.comparing(WorkflowTaskRecordEntity::getIndex))
                    .orElse(resetTasks.get(0));
            syncInstanceOnForceRetry(dispatchTask);
            registerForceRetryMq(dispatchTask);
            mqCount++;
        }
        if (resetCount == 0) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NO_ELIGIBLE);
        }
        addForceRetryLog(dto, taskList, resetCount, mqCount);

        WorkflowTaskRecordEntity first = taskList.get(0);
        WorkflowTaskRecordDTO.ForceRetryResultDTO resultDTO = new WorkflowTaskRecordDTO.ForceRetryResultDTO();
        resultDTO.setSourceType(first.getSourceType());
        resultDTO.setSourceId(first.getSourceId());
        resultDTO.setResetCount(resetCount);
        resultDTO.setScheduledMqCount(mqCount);
        resultDTO.setMqCount(mqCount);
        return resultDTO;
    }

    /**
     * 逻辑删除指定业务单据下的全部节点记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBySourceIdAndSourceType(String sourceId, String sourceType) {
        if (CharSequenceUtil.isAllNotBlank(sourceId, sourceType)) {
            this.lambdaUpdate()
                    .eq(WorkflowTaskRecordEntity::getSourceId, sourceId)
                    .eq(WorkflowTaskRecordEntity::getSourceType, sourceType)
                    .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                    .set(WorkflowTaskRecordEntity::getIsDeleted, true)
                    .update();
        }
    }

    /**
     * 查询指定业务单据下的全部有效节点（按 index 升序）。
     */
    @Override
    public List<WorkflowTaskRecordEntity> listBySourceId(String soId, String sourceType) {
        if (CharSequenceUtil.isBlank(soId)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery()
                .eq(WorkflowTaskRecordEntity::getSourceId, soId)
                .eq(WorkflowTaskRecordEntity::getSourceType, sourceType)
                .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                .orderByAsc(WorkflowTaskRecordEntity::getIndex)
                .list();
    }

    /** {@inheritDoc} */
    @Override
    public List<WorkflowTaskRecordEntity> listLegacyBySourceId(String sourceId, String sourceType) {
        return listLegacyTasksBySource(sourceId, sourceType);
    }

    /**
     * 获取 source + index 对应的最新有效节点记录。
     */
    @Override
    public WorkflowTaskRecordEntity getActiveTask(String sourceId, String sourceType, Integer index) {
        if (CharSequenceUtil.isBlank(sourceId) || CharSequenceUtil.isBlank(sourceType) || Objects.isNull(index)) {
            return null;
        }
        return this.lambdaQuery()
                .eq(WorkflowTaskRecordEntity::getSourceId, sourceId)
                .eq(WorkflowTaskRecordEntity::getSourceType, sourceType)
                .eq(WorkflowTaskRecordEntity::getIndex, index)
                .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                .orderByDesc(WorkflowTaskRecordEntity::getCreateTime)
                .last("limit 1")
                .one();
    }

    /** {@inheritDoc} */
    @Override
    public Boolean resetStaleProcessingTask(String id) {
        if (CharSequenceUtil.isBlank(id)) {
            return Boolean.FALSE;
        }
        WorkflowTaskRecordEntity entity = getById(id);
        if (Objects.isNull(entity)
                || !Objects.equals(entity.getStatus(), WorkflowTaskRecordStatusEnum.PROCESSING.getCode())) {
            return Boolean.FALSE;
        }
        LocalDateTime updateTime = entity.getUpdateTime();
        if (Objects.nonNull(updateTime)
                && updateTime.plusMinutes(TASK_PROCESSING_TIMEOUT_MINUTES).isAfter(LocalDateTime.now())) {
            return Boolean.FALSE;
        }
        return this.lambdaUpdate()
                .eq(WorkflowTaskRecordEntity::getId, id)
                .eq(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.PROCESSING.getCode())
                .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.PENDING.getCode())
                .update();
    }

    /** {@inheritDoc} */
    @Override
    public Boolean claimTask(String id, String fromStatus) {
        if (CharSequenceUtil.isBlank(id) || CharSequenceUtil.isBlank(fromStatus)) {
            return Boolean.FALSE;
        }
        return this.lambdaUpdate()
                .eq(WorkflowTaskRecordEntity::getId, id)
                .eq(WorkflowTaskRecordEntity::getStatus, fromStatus)
                .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.PROCESSING.getCode())
                .update();
    }

    /**
     * 根据重试参数解析候选节点列表：支持按节点 ID 或按 source 维度查询。
     */
    private List<WorkflowTaskRecordEntity> listForceRetryTasks(WorkflowTaskRecordDTO.ForceRetryDTO dto) {
        if (CharSequenceUtil.isNotBlank(dto.getId())) {
            WorkflowTaskRecordEntity entity = getById(dto.getId());
            if (Objects.isNull(entity)) {
                return Collections.emptyList();
            }
            return listTasksByInstanceScope(entity.getInstanceId(), entity.getSourceId(), entity.getSourceType());
        }
        if (CharSequenceUtil.isBlank(dto.getSourceType()) || CharSequenceUtil.isBlank(dto.getSourceId())) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_PARAM_INCOMPLETE);
        }
        return listTasksByInstanceScope(dto.getInstanceId(), dto.getSourceId(), dto.getSourceType());
    }

    /**
     * 按实例维度查询待重试节点，避免同一 source 多轮编排时误重置历史实例节点。
     */
    private List<WorkflowTaskRecordEntity> listTasksByInstanceScope(String instanceId, String sourceId, String sourceType) {
        String resolvedInstanceId = instanceId;
        if (CharSequenceUtil.isBlank(resolvedInstanceId)) {
            WorkflowTaskInstanceEntity instance = workflowTaskInstanceService.getLatestBySource(sourceId, sourceType);
            resolvedInstanceId = instance == null ? null : instance.getId();
        }
        if (CharSequenceUtil.isNotBlank(resolvedInstanceId)) {
            return this.lambdaQuery()
                    .eq(WorkflowTaskRecordEntity::getInstanceId, resolvedInstanceId)
                    .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                    .orderByAsc(WorkflowTaskRecordEntity::getIndex)
                    .list();
        }
        return listLegacyTasksBySource(sourceId, sourceType);
    }

    /**
     * 无实例 ID 的历史数据：仅查询未绑定 instance 的节点，避免多轮编排混用。
     */
    private List<WorkflowTaskRecordEntity> listLegacyTasksBySource(String sourceId, String sourceType) {
        return this.lambdaQuery()
                .eq(WorkflowTaskRecordEntity::getSourceId, sourceId)
                .eq(WorkflowTaskRecordEntity::getSourceType, sourceType)
                .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                .and(w -> w.eq(WorkflowTaskRecordEntity::getInstanceId, "")
                        .or()
                        .isNull(WorkflowTaskRecordEntity::getInstanceId))
                .orderByAsc(WorkflowTaskRecordEntity::getIndex)
                .list();
    }

    /**
     * 补偿链式 MQ 漏发：实例 running 且 current_index 已成功、下一节点 pending/failed 时补发单步 MQ。
     */
    private void compensateStuckChainSteps() {
        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(TASK_PROCESSING_TIMEOUT_MINUTES);
        List<WorkflowTaskInstanceEntity> runningInstances = workflowTaskInstanceService.lambdaQuery()
                .eq(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.RUNNING.getCode())
                .eq(WorkflowTaskInstanceEntity::getIsDeleted, false)
                .lt(WorkflowTaskInstanceEntity::getUpdateTime, staleBefore)
                .list();
        if (CollUtil.isEmpty(runningInstances)) {
            return;
        }
        for (WorkflowTaskInstanceEntity instance : runningInstances) {
            List<WorkflowTaskRecordEntity> steps = this.lambdaQuery()
                    .eq(WorkflowTaskRecordEntity::getInstanceId, instance.getId())
                    .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                    .orderByAsc(WorkflowTaskRecordEntity::getIndex)
                    .list();
            if (CollUtil.isEmpty(steps)) {
                continue;
            }
            Map<Integer, WorkflowTaskRecordEntity> indexMap = buildIndexTaskMap(steps);
            WorkflowTaskRecordEntity current = indexMap.get(instance.getCurrentIndex());
            if (current == null
                    || !WorkflowTaskRecordStatusEnum.SUCCESS.getCode().equals(current.getStatus())) {
                continue;
            }
            int nextIndex = instance.getCurrentIndex() + 1;
            WorkflowTaskRecordEntity next = indexMap.get(nextIndex);
            if (next == null) {
                continue;
            }
            if (!WorkflowTaskRecordStatusEnum.PENDING.getCode().equals(next.getStatus())
                    && !WorkflowTaskRecordStatusEnum.FAILED.getCode().equals(next.getStatus())) {
                continue;
            }
            WorkflowTaskRecordDTO.AddTaskDTO template = new WorkflowTaskRecordDTO.AddTaskDTO();
            template.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE);
            template.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.getByCode(instance.getSourceType()));
            template.setTraceId(instance.getTraceId());
            template.setTargetIndex(nextIndex);
            WorkflowTaskRecordDTO.AddTaskDTO dispatch = buildDispatchDto(instance, template);
            dispatch.setRetryFailedStep(WorkflowTaskRecordStatusEnum.FAILED.getCode().equals(next.getStatus()));
            try {
                workflowTaskStepDispatcher.sendDispatchMq(dispatch, instance.getSourceId());
                XxlJobHelper.log(StrUtil.format("链式调度补偿成功，instanceId={}, nextIndex={}", instance.getId(), nextIndex));
            } catch (Exception ex) {
                log.warn("链式调度补偿 MQ 失败，instanceId={}, nextIndex={}", instance.getId(), nextIndex, ex);
                XxlJobHelper.log(StrUtil.format("链式调度补偿 MQ 失败，instanceId={}, nextIndex={}, error={}",
                        instance.getId(), nextIndex, ex.getMessage()));
            }
        }
    }

    /**
     * 重置单个强制重试节点状态，并尽可能回填上一成功节点 output 到 input。
     */
    private void resetForceRetryTask(WorkflowTaskRecordEntity entity, WorkflowTaskRecordDTO.ForceRetryDTO dto, Map<Integer, WorkflowTaskRecordEntity> indexTaskMap) {
        String remark = appendForceRetryRemark(entity.getRemark(), dto.getRemark());
        String refreshedInputData = getPreviousSuccessOutputDataInternal(entity, indexTaskMap);
        if (CharSequenceUtil.isNotBlank(refreshedInputData)) {
            this.lambdaUpdate()
                    .eq(WorkflowTaskRecordEntity::getId, entity.getId())
                    .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.PENDING.getCode())
                    .set(WorkflowTaskRecordEntity::getRetryCount, Optional.ofNullable(dto.getRetryCount()).orElse(0))
                    .set(WorkflowTaskRecordEntity::getLastError, "")
                    .set(WorkflowTaskRecordEntity::getRemark, remark)
                    .set(WorkflowTaskRecordEntity::getInputData, refreshedInputData)
                    .update();
            return;
        }
        this.lambdaUpdate()
                .eq(WorkflowTaskRecordEntity::getId, entity.getId())
                .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.PENDING.getCode())
                .set(WorkflowTaskRecordEntity::getRetryCount, Optional.ofNullable(dto.getRetryCount()).orElse(0))
                .set(WorkflowTaskRecordEntity::getLastError, "")
                .set(WorkflowTaskRecordEntity::getRemark, remark)
                .update();
    }

    /**
     * 以 index 构建节点映射，过滤空节点和逻辑删除节点。
     */
    private Map<Integer, WorkflowTaskRecordEntity> buildIndexTaskMap(List<WorkflowTaskRecordEntity> taskList) {
        return CollUtil.emptyIfNull(taskList).stream()
                .filter(Objects::nonNull)
                .filter(e -> Objects.nonNull(e.getIndex()))
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .collect(Collectors.toMap(WorkflowTaskRecordEntity::getIndex, e -> e, (o1, o2) -> o1));
    }

    /**
     * 获取前一节点成功输出，作为当前节点重试入参。
     */
    private String getPreviousSuccessOutputDataInternal(WorkflowTaskRecordEntity entity, Map<Integer, WorkflowTaskRecordEntity> indexTaskMap) {
        if (Objects.isNull(entity)
                || Objects.isNull(entity.getIndex())
                || entity.getIndex() <= 0
                || Objects.isNull(indexTaskMap)) {
            return null;
        }
        WorkflowTaskRecordEntity previousTask = indexTaskMap.get(entity.getIndex() - 1);
        if (Objects.isNull(previousTask)
                || !Objects.equals(previousTask.getStatus(), WorkflowTaskRecordStatusEnum.SUCCESS.getCode())
                || Boolean.TRUE.equals(previousTask.getIsDeleted())) {
            return null;
        }
        return previousTask.getOutputData();
    }

    /**
     * 判断节点是否可强制重试：成功节点不允许，PROCESSING 需超时。
     */
    private boolean allowForceRetry(WorkflowTaskRecordEntity entity) {
        if (Objects.equals(entity.getStatus(), WorkflowTaskRecordStatusEnum.SUCCESS.getCode())) {
            return false;
        }
        if (!Objects.equals(entity.getStatus(), WorkflowTaskRecordStatusEnum.PROCESSING.getCode())) {
            return true;
        }
        LocalDateTime updateTime = entity.getUpdateTime();
        // 历史 PROCESSING 节点可能没有可靠更新时间，按超时处理，允许人工/Job 重新调度。
        return Objects.isNull(updateTime) || updateTime.plusMinutes(TASK_PROCESSING_TIMEOUT_MINUTES).isBefore(LocalDateTime.now());
    }

    /**
     * 追加强制重试备注，保留历史备注并附加操作人信息。
     */
    private String appendForceRetryRemark(String oldRemark, String remark) {
        String operator = UserContext.getDefaultLoginUser().getUserName();
        String current = StrUtil.format("人工强制重试，操作人：{}，备注：{}", operator, StrUtil.blankToDefault(remark, ""));
        if (CharSequenceUtil.isBlank(oldRemark)) {
            return current;
        }
        return oldRemark + "\n" + current;
    }

    /** 强制重试后将实例状态同步为 running，并指向即将调度的节点 index。 */
    private void syncInstanceOnForceRetry(WorkflowTaskRecordEntity dispatchTask) {
        WorkflowTaskInstanceEntity instance = CharSequenceUtil.isNotBlank(dispatchTask.getInstanceId())
                ? workflowTaskInstanceService.getById(dispatchTask.getInstanceId())
                : workflowTaskInstanceService.getLatestBySource(dispatchTask.getSourceId(), dispatchTask.getSourceType());
        if (instance == null) {
            return;
        }
        int totalSteps = Optional.ofNullable(instance.getTotalSteps()).orElse(0);
        if (totalSteps <= 0) {
            totalSteps = (int) this.lambdaQuery()
                    .eq(WorkflowTaskRecordEntity::getInstanceId, instance.getId())
                    .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                    .count();
        }
        workflowTaskInstanceService.markRunning(instance.getId(), dispatchTask.getIndex(), totalSteps);
    }

    /**
     * 发送强制重试单步调度 MQ。
     */
    private Boolean sendForceRetryMq(WorkflowTaskRecordEntity entity) {
        WorkflowTaskRecordTypeEnum sourceTypeEnum = WorkflowTaskRecordTypeEnum.getByCode(entity.getSourceType());
        if (Objects.isNull(sourceTypeEnum)) {
            log.warn("任务节点人工强制重试失败，未知sourceType={}", entity.getSourceType());
            return Boolean.FALSE;
        }
        WorkflowTaskInstanceEntity instance = CharSequenceUtil.isNotBlank(entity.getInstanceId())
                ? workflowTaskInstanceService.getById(entity.getInstanceId())
                : workflowTaskInstanceService.getLatestBySource(entity.getSourceId(), entity.getSourceType());
        WorkflowTaskRecordDTO.AddTaskDTO template = new WorkflowTaskRecordDTO.AddTaskDTO();
        template.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE);
        template.setSourceTypeEnum(sourceTypeEnum);
        template.setTraceId(entity.getTraceId());
        template.setTargetIndex(entity.getIndex());
        template.setSourceId(entity.getSourceId());
        template.setSourceCode(entity.getSourceCode());
        if (CharSequenceUtil.isNotBlank(entity.getInstanceId())) {
            template.setInstanceId(entity.getInstanceId());
        }
        WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = buildDispatchDto(instance, template);
        addTaskDTO.setRetryFailedStep(Boolean.TRUE);
        addTaskDTO.setForceRetry(Boolean.TRUE);
        workflowTaskStepDispatcher.sendDispatchMq(addTaskDTO, entity.getSourceId());
        return Boolean.TRUE;
    }

    /**
     * 在事务提交后注册强制重试 MQ 发送，避免读取未提交数据。
     */
    private void registerForceRetryMq(WorkflowTaskRecordEntity entity) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    try {
                        sendForceRetryMq(entity);
                    } catch (Exception e) {
                        log.error("任务节点人工强制重试事务提交后发送MQ失败，sourceType={}, sourceId={}, traceId={}",
                                entity.getSourceType(), entity.getSourceId(), entity.getTraceId(), e);
                        markForceRetryMqSendFailed(entity, e);
                    }
                }
            });
            return;
        }
        sendForceRetryMq(entity);
    }

    /**
     * 强制重试 MQ 发送失败时，回写最早未成功节点为 FAILED 并记录错误。
     */
    private void markForceRetryMqSendFailed(WorkflowTaskRecordEntity entity, Exception e) {
        if (Objects.isNull(entity) || CharSequenceUtil.isBlank(entity.getSourceId()) || CharSequenceUtil.isBlank(entity.getSourceType())) {
            return;
        }
        WorkflowTaskRecordEntity taskEntity = this.lambdaQuery()
                .eq(WorkflowTaskRecordEntity::getSourceId, entity.getSourceId())
                .eq(WorkflowTaskRecordEntity::getSourceType, entity.getSourceType())
                .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                .ne(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.SUCCESS.getCode())
                .orderByAsc(WorkflowTaskRecordEntity::getIndex)
                .last("limit 1")
                .one();
        if (Objects.isNull(taskEntity)) {
            return;
        }
        String errorMsg = StrUtil.format("任务节点人工强制重试MQ发送失败，traceId={}，error={}",
                entity.getTraceId(),
                Objects.nonNull(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName());
        this.lambdaUpdate()
                .eq(WorkflowTaskRecordEntity::getId, taskEntity.getId())
                .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.FAILED.getCode())
                .set(WorkflowTaskRecordEntity::getLastError, errorMsg)
                .set(WorkflowTaskRecordEntity::getRetryCount, Optional.ofNullable(taskEntity.getRetryCount()).orElse(0) + 1)
                .update();
    }

    /**
     * 写入人工强制重试操作日志。
     */
    private void addForceRetryLog(WorkflowTaskRecordDTO.ForceRetryDTO dto, List<WorkflowTaskRecordEntity> taskList, int resetCount, int mqCount) {
        WorkflowTaskRecordEntity first = taskList.get(0);
        String content = StrUtil.format("用户【{}】人工强制重试任务节点，sourceType=【{}】，sourceId=【{}】，重置节点数=【{}】，调度MQ数=【{}】，备注=【{}】",
                UserContext.getDefaultLoginUser().getUserName(),
                first.getSourceType(),
                first.getSourceId(),
                resetCount,
                mqCount,
                StrUtil.blankToDefault(dto.getRemark(), ""));
        operateLogService.addModuleOperateLog(content, resolveModuleType(first), resolveBusinessId(first), "任务强制重试");
    }

    /**
     * 对外暴露强制重试权限校验能力。
     */
    @Override
    public void validateForceRetryPermission() {
        checkForceRetryPermission();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isStepForceRetryAllowed(WorkflowTaskRecordEntity entity) {
        return allowForceRetry(entity);
    }

    /** {@inheritDoc} */
    @Override
    public String formatForceRetryRemark(String oldRemark, String remark) {
        return appendForceRetryRemark(oldRemark, remark);
    }

    /** {@inheritDoc} */
    @Override
    public String getPreviousSuccessOutputData(WorkflowTaskRecordEntity entity,
                                               Map<Integer, WorkflowTaskRecordEntity> indexTaskMap) {
        return getPreviousSuccessOutputDataInternal(entity, indexTaskMap);
    }

    /**
     * 校验当前用户是否拥有人工强制重试权限（超级管理员或菜单权限）。
     */
    private void checkForceRetryPermission() {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        if (Objects.nonNull(loginUser) && Boolean.TRUE.equals(loginUser.getIsSupper())) {
            return;
        }
        List<String> permissionList = Objects.isNull(loginUser) ? Collections.emptyList() : loginUser.getPermissionList();
        if (CollUtil.isNotEmpty(permissionList) && permissionList.contains(FORCE_RETRY_PERMISSION)) {
            return;
        }
        throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_FORBIDDEN);
    }

    /**
     * 根据 sourceType 解析操作日志模块编码。
     */
    private String resolveModuleType(WorkflowTaskRecordEntity entity) {
        if (Objects.equals(entity.getSourceType(), WorkflowTaskRecordTypeEnum.KOL_B2C_APPLICATION_APPROVE.getCode())
                || Objects.equals(entity.getSourceType(), WorkflowTaskRecordTypeEnum.KOL_B2C_SUB_APPROVE.getCode())) {
            return ModuleTypeEnum.KOL_B2C_APPLICATION.getCode();
        }
        return null;
    }

    /**
     * 解析日志业务主键（子申请场景回溯到主申请 ID）。
     */
    private String resolveBusinessId(WorkflowTaskRecordEntity entity) {
        if (Objects.equals(entity.getSourceType(), WorkflowTaskRecordTypeEnum.KOL_B2C_SUB_APPROVE.getCode())) {
            KolSubB2cApplicationEntity subEntity = kolSubB2cApplicationService.getById(entity.getSourceId());
            if (Objects.nonNull(subEntity) && CharSequenceUtil.isNotBlank(subEntity.getSourceId())) {
                return subEntity.getSourceId();
            }
        }
        return entity.getSourceId();
    }

    /** 组装单步调度 MQ 消息体，优先使用实例上的 source/trace 信息。 */
    private WorkflowTaskRecordDTO.AddTaskDTO buildDispatchDto(WorkflowTaskInstanceEntity instance,
                                                              WorkflowTaskRecordDTO.AddTaskDTO template) {
        WorkflowTaskRecordDTO.AddTaskDTO dto = new WorkflowTaskRecordDTO.AddTaskDTO();
        if (instance != null) {
            dto.setInstanceId(instance.getId());
            dto.setSourceId(instance.getSourceId());
            dto.setSourceCode(instance.getSourceCode());
            dto.setTraceId(CharSequenceUtil.blankToDefault(template.getTraceId(), instance.getTraceId()));
            dto.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.getByCode(instance.getSourceType()));
        } else {
            dto.setInstanceId(template.getInstanceId());
            dto.setSourceId(template.getSourceId());
            dto.setSourceCode(template.getSourceCode());
            dto.setTraceId(template.getTraceId());
            dto.setSourceTypeEnum(template.getSourceTypeEnum());
        }
        dto.setDictBasicTypeEnum(template.getDictBasicTypeEnum());
        dto.setTargetIndex(template.getTargetIndex());
        return dto;
    }

    /** 事务提交后再发 MQ，避免消费端读不到未提交节点。 */
    private void registerDispatchAfterCommit(WorkflowTaskRecordDTO.AddTaskDTO dispatch, String messageKey) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    try {
                        workflowTaskStepDispatcher.sendDispatchMq(dispatch, messageKey);
                    } catch (Exception ex) {
                        log.error("任务编排 MQ 发送失败（事务已提交），sourceId={}, instanceId={}, targetIndex={}",
                                dispatch.getSourceId(), dispatch.getInstanceId(), dispatch.getTargetIndex(), ex);
                    }
                }
            });
            return;
        }
        workflowTaskStepDispatcher.sendDispatchMq(dispatch, messageKey);
    }
}
