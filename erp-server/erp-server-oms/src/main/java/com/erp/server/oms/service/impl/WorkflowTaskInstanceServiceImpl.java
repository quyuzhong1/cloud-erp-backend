package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.DistributeKeyConstant;
import com.erp.model.oms.dto.WorkflowTaskInstanceDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskInstanceEntity;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.WorkflowTaskInstanceStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import com.erp.server.oms.mapper.WorkflowTaskInstanceMapper;
import com.erp.server.oms.orchestration.StepInvokeResult;
import com.erp.server.oms.orchestration.WorkflowTaskNodeConfigParser;
import com.erp.server.oms.orchestration.WorkflowTaskStepDispatcher;
import com.erp.server.oms.service.WorkflowTaskInstanceService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务编排实例服务实现：维护实例状态机，并提供运维监控查询与重试能力。
 */
@Slf4j
@Service
public class WorkflowTaskInstanceServiceImpl extends SuperServiceImpl<WorkflowTaskInstanceMapper, WorkflowTaskInstanceEntity>
        implements WorkflowTaskInstanceService {

    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;

    @Lazy
    @Resource
    private WorkflowTaskStepDispatcher workflowTaskStepDispatcher;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTaskInstanceEntity createInstance(WorkflowTaskRecordDTO.AddTaskDTO dto, int totalSteps) {
        WorkflowTaskInstanceEntity entity = new WorkflowTaskInstanceEntity();
        entity.setId(IdWorker.getIdStr());
        entity.setSourceType(dto.getSourceTypeEnum().getCode());
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(CharSequenceUtil.blankToDefault(dto.getSourceCode(), ""));
        entity.setStatus(WorkflowTaskInstanceStatusEnum.RUNNING.getCode());
        entity.setCurrentIndex(0);
        entity.setTotalSteps(totalSteps);
        entity.setTraceId(CharSequenceUtil.blankToDefault(dto.getTraceId(), ""));
        entity.setStartTime(LocalDateTime.now());
        entity.setLastError("");
        save(entity);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowTaskInstanceEntity getLatestBySource(String sourceId, String sourceType) {
        if (CharSequenceUtil.hasBlank(sourceId, sourceType)) {
            return null;
        }
        return this.lambdaQuery()
                .eq(WorkflowTaskInstanceEntity::getSourceId, sourceId)
                .eq(WorkflowTaskInstanceEntity::getSourceType, sourceType)
                .eq(WorkflowTaskInstanceEntity::getIsDeleted, false)
                .orderByDesc(WorkflowTaskInstanceEntity::getCreateTime)
                .last("limit 1")
                .one();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTaskInstanceEntity ensureInstanceForLegacy(WorkflowTaskRecordDTO.AddTaskDTO dto,
                                                              List<WorkflowTaskRecordEntity> steps) {
        if (CollUtil.isEmpty(steps)) {
            return null;
        }
        // 必须先回拨历史 PROCESSING，再回填 instance_id；回填会刷新 update_time，影响超时判断。
        prepareLegacyProcessingSteps(steps);
        WorkflowTaskInstanceEntity existed = getLatestBySource(dto.getSourceId(), dto.getSourceTypeEnum().getCode());
        if (existed != null) {
            linkStepsToInstance(steps, existed.getId());
            if (Optional.ofNullable(existed.getTotalSteps()).orElse(0) <= 0) {
                this.lambdaUpdate()
                        .eq(WorkflowTaskInstanceEntity::getId, existed.getId())
                        .set(WorkflowTaskInstanceEntity::getTotalSteps, steps.size())
                        .update();
                existed.setTotalSteps(steps.size());
            }
            return existed;
        }
        WorkflowTaskInstanceEntity instance = createInstance(dto, steps.size());
        inferInstanceStatus(instance, steps);
        linkStepsToInstance(steps, instance.getId());
        return instance;
    }

    private void linkStepsToInstance(List<WorkflowTaskRecordEntity> steps, String instanceId) {
        List<String> ids = steps.stream()
                .filter(s -> CharSequenceUtil.isBlank(s.getInstanceId()))
                .map(WorkflowTaskRecordEntity::getId)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        // 分批（每批 500 条）批量回填 instance_id，避免 N+1 单条 UPDATE
        for (List<String> batch : Lists.partition(ids, 500)) {
            workflowTaskRecordService.lambdaUpdate()
                    .in(WorkflowTaskRecordEntity::getId, batch)
                    .set(WorkflowTaskRecordEntity::getInstanceId, instanceId)
                    .update();
        }
        steps.forEach(s -> {
            if (CharSequenceUtil.isBlank(s.getInstanceId())) {
                s.setInstanceId(instanceId);
            }
        });
    }

    /**
     * 历史节点在回填 instance_id 前先释放已超时的 PROCESSING，避免回填时刷新 update_time 后被误判为未超时。
     */
    private void prepareLegacyProcessingSteps(List<WorkflowTaskRecordEntity> steps) {
        for (WorkflowTaskRecordEntity step : steps) {
            if (!WorkflowTaskRecordStatusEnum.PROCESSING.getCode().equals(step.getStatus())) {
                continue;
            }
            if (Boolean.TRUE.equals(workflowTaskRecordService.resetStaleProcessingTask(step.getId()))) {
                step.setStatus(WorkflowTaskRecordStatusEnum.PENDING.getCode());
            }
        }
    }

    private void inferInstanceStatus(WorkflowTaskInstanceEntity instance, List<WorkflowTaskRecordEntity> steps) {
        boolean allSuccess = steps.stream()
                .allMatch(e -> WorkflowTaskRecordStatusEnum.SUCCESS.getCode().equals(e.getStatus()));
        if (allSuccess) {
            markSuccess(instance.getId(),
                    steps.stream().map(WorkflowTaskRecordEntity::getIndex).max(Integer::compareTo).orElse(0),
                    steps.size());
            return;
        }
        WorkflowTaskRecordEntity current = steps.stream()
                .filter(e -> !WorkflowTaskRecordStatusEnum.SUCCESS.getCode().equals(e.getStatus()))
                .min(Comparator.comparing(WorkflowTaskRecordEntity::getIndex))
                .orElse(steps.get(0));
        if (WorkflowTaskRecordStatusEnum.WAITING.getCode().equals(current.getStatus())) {
            markWaiting(instance.getId(), current.getIndex(), current.getLastError());
        } else if (WorkflowTaskRecordStatusEnum.FAILED.getCode().equals(current.getStatus())) {
            markFailed(instance.getId(), current.getIndex(), current.getLastError());
        } else {
            markRunning(instance.getId(), current.getIndex(), steps.size());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markRunning(String instanceId, int currentIndex, int totalSteps) {
        WorkflowTaskInstanceEntity fresh = getById(instanceId);
        if (fresh == null) {
            log.warn("markRunning 跳过，实例不存在，instanceId={}", instanceId);
            return;
        }
        boolean updated = this.lambdaUpdate()
                .eq(WorkflowTaskInstanceEntity::getId, instanceId)
                .eq(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion())
                .ne(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.CANCELLED.getCode())
                .ne(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.SUCCESS.getCode())
                .set(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.RUNNING.getCode())
                .set(WorkflowTaskInstanceEntity::getCurrentIndex, currentIndex)
                .set(WorkflowTaskInstanceEntity::getTotalSteps, totalSteps)
                .set(WorkflowTaskInstanceEntity::getLastError, "")
                .set(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion() + 1)
                .update();
        if (!updated) {
            log.warn("markRunning 并发冲突，instanceId={}, version={}", instanceId, fresh.getVersion());
        }
    }

    /**
     * 人工重试场景下实例状态必须成功切回 RUNNING 后才能发送 MQ；
     * 否则会出现实例未切换但节点已重置、MQ 已发送的不一致状态。
     */
    @Override
    public void markRunningOrThrow(String instanceId, int currentIndex, int totalSteps) {
        WorkflowTaskInstanceEntity fresh = getById(instanceId);
        if (fresh == null) {
            throw new ServiceException(ApiError.WF_TASK_INSTANCE_NOT_FOUND);
        }
        boolean updated = this.lambdaUpdate()
                .eq(WorkflowTaskInstanceEntity::getId, instanceId)
                .eq(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion())
                .ne(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.CANCELLED.getCode())
                .ne(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.SUCCESS.getCode())
                .set(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.RUNNING.getCode())
                .set(WorkflowTaskInstanceEntity::getCurrentIndex, currentIndex)
                .set(WorkflowTaskInstanceEntity::getTotalSteps, totalSteps)
                .set(WorkflowTaskInstanceEntity::getLastError, "")
                .set(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion() + 1)
                .update();
        if (!updated) {
            log.warn("markRunningOrThrow 并发冲突，instanceId={}, version={}", instanceId, fresh.getVersion());
            throw new ServiceException(ApiError.WF_TASK_INSTANCE_VERSION_CONFLICT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markWaiting(String instanceId, int currentIndex, String lastError) {
        WorkflowTaskInstanceEntity fresh = getById(instanceId);
        if (fresh == null) {
            log.warn("markWaiting 跳过，实例不存在，instanceId={}", instanceId);
            return;
        }
        boolean updated = this.lambdaUpdate()
                .eq(WorkflowTaskInstanceEntity::getId, instanceId)
                .eq(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion())
                .ne(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.CANCELLED.getCode())
                .ne(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.SUCCESS.getCode())
                .set(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.WAITING.getCode())
                .set(WorkflowTaskInstanceEntity::getCurrentIndex, currentIndex)
                .set(WorkflowTaskInstanceEntity::getLastError, CharSequenceUtil.blankToDefault(lastError, ""))
                .set(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion() + 1)
                .update();
        if (!updated) {
            log.warn("markWaiting 并发冲突，instanceId={}, version={}", instanceId, fresh.getVersion());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markFailed(String instanceId, int currentIndex, String lastError) {
        WorkflowTaskInstanceEntity fresh = getById(instanceId);
        if (fresh == null) {
            log.warn("markFailed 跳过，实例不存在，instanceId={}", instanceId);
            return;
        }
        boolean updated = this.lambdaUpdate()
                .eq(WorkflowTaskInstanceEntity::getId, instanceId)
                .eq(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion())
                .ne(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.CANCELLED.getCode())
                .set(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.FAILED.getCode())
                .set(WorkflowTaskInstanceEntity::getCurrentIndex, currentIndex)
                .set(WorkflowTaskInstanceEntity::getLastError, CharSequenceUtil.blankToDefault(lastError, ""))
                .set(WorkflowTaskInstanceEntity::getFinishTime, LocalDateTime.now())
                .set(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion() + 1)
                .update();
        if (!updated) {
            log.warn("markFailed 并发冲突，instanceId={}, version={}", instanceId, fresh.getVersion());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markSuccess(String instanceId, int currentIndex, int totalSteps) {
        WorkflowTaskInstanceEntity fresh = getById(instanceId);
        if (fresh == null) {
            log.warn("markSuccess 跳过，实例不存在，instanceId={}", instanceId);
            return;
        }
        boolean updated = this.lambdaUpdate()
                .eq(WorkflowTaskInstanceEntity::getId, instanceId)
                .eq(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion())
                .ne(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.CANCELLED.getCode())
                .set(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.SUCCESS.getCode())
                .set(WorkflowTaskInstanceEntity::getCurrentIndex, currentIndex)
                .set(WorkflowTaskInstanceEntity::getTotalSteps, totalSteps)
                .set(WorkflowTaskInstanceEntity::getLastError, "")
                .set(WorkflowTaskInstanceEntity::getFinishTime, LocalDateTime.now())
                .set(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion() + 1)
                .update();
        if (!updated) {
            log.warn("markSuccess 并发冲突，instanceId={}, version={}", instanceId, fresh.getVersion());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCancelled(String instanceId, String remark) {
        WorkflowTaskInstanceEntity fresh = getById(instanceId);
        if (fresh == null) {
            throw new ServiceException(ApiError.WF_TASK_INSTANCE_NOT_FOUND);
        }
        boolean updated = this.lambdaUpdate()
                .eq(WorkflowTaskInstanceEntity::getId, instanceId)
                .eq(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion())
                .set(WorkflowTaskInstanceEntity::getStatus, WorkflowTaskInstanceStatusEnum.CANCELLED.getCode())
                .set(WorkflowTaskInstanceEntity::getLastError, CharSequenceUtil.blankToDefault(remark, "已取消"))
                .set(WorkflowTaskInstanceEntity::getFinishTime, LocalDateTime.now())
                .set(WorkflowTaskInstanceEntity::getVersion, fresh.getVersion() + 1)
                .update();
        if (!updated) {
            // 并发取消冲突：实例已被其他请求更新，跳过子节点批量修改，避免实例/节点状态不一致
            log.warn("markCancelled 并发冲突，instanceId={}, version={}", instanceId, fresh.getVersion());
            throw new ServiceException(ApiError.WF_TASK_INSTANCE_VERSION_CONFLICT);
        }
        workflowTaskRecordService.lambdaUpdate()
                .eq(WorkflowTaskRecordEntity::getInstanceId, instanceId)
                .ne(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.SUCCESS.getCode())
                .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.FAILED.getCode())
                .set(WorkflowTaskRecordEntity::getLastError, "实例已取消")
                .update();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagingVO<WorkflowTaskInstanceDTO.ListDTO> paging(PagingDTO<WorkflowTaskInstanceDTO.PagingParamDTO> dto) {
        WorkflowTaskInstanceDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<Object> page = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<WorkflowTaskInstanceDTO.ListDTO> pageData = baseMapper.paging(page, params);
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        pageData.getRecords().forEach(this::fillListNames);
        return new PagingVO<>(pageData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowTaskInstanceDTO.ViewDTO view(String id) {
        WorkflowTaskInstanceDTO.ViewDTO view = baseMapper.viewHeader(id);
        if (view == null) {
            throw new ServiceException(ApiError.WF_TASK_INSTANCE_NOT_FOUND);
        }
        fillViewNames(view);
        List<WorkflowTaskInstanceDTO.StepDTO> steps = baseMapper.listSteps(id);
        fillStepNames(steps);
        view.setSteps(steps);
        fillAutoRetryExceeded(view, steps);
        if (view.getTotalSteps() != null && view.getTotalSteps() > 0 && CollUtil.isNotEmpty(steps)) {
            long successCount = steps.stream()
                    .filter(s -> WorkflowTaskRecordStatusEnum.SUCCESS.getCode().equals(s.getStatus()))
                    .count();
            view.setProgressPercent((int) Math.min(100, successCount * 100 / view.getTotalSteps()));
        }
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorkflowTaskInstanceDTO.ViewDTO> listBySource(WorkflowTaskInstanceDTO.ListBySourceParamDTO param) {
        if (param == null || CharSequenceUtil.hasBlank(param.getSourceType(), param.getSourceId())) {
            return Collections.emptyList();
        }
        List<String> instanceIds = baseMapper.listBySource(param);
        if (CollUtil.isEmpty(instanceIds)) {
            return Collections.emptyList();
        }
        List<WorkflowTaskInstanceDTO.ViewDTO> headers = baseMapper.listViewHeaders(instanceIds);
        if (CollUtil.isEmpty(headers)) {
            return Collections.emptyList();
        }
        Map<String, List<WorkflowTaskInstanceDTO.StepDTO>> stepsMap = baseMapper.listStepsByInstanceIds(instanceIds).stream()
                .collect(Collectors.groupingBy(WorkflowTaskInstanceDTO.StepDTO::getInstanceId));
        return headers.stream().map(header -> {
            fillViewNames(header);
            List<WorkflowTaskInstanceDTO.StepDTO> steps = stepsMap.getOrDefault(header.getInstanceId(), Collections.emptyList());
            fillStepNames(steps);
            header.setSteps(steps);
            fillAutoRetryExceeded(header, steps);
            if (header.getTotalSteps() != null && header.getTotalSteps() > 0 && CollUtil.isNotEmpty(steps)) {
                long successCount = steps.stream()
                        .filter(s -> WorkflowTaskRecordStatusEnum.SUCCESS.getCode().equals(s.getStatus()))
                        .count();
                header.setProgressPercent((int) Math.min(100, successCount * 100 / header.getTotalSteps()));
            }
            return header;
        }).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorkflowTaskInstanceDTO.ErrorReportDTO> errorReport(WorkflowTaskInstanceDTO.ErrorReportParamDTO param) {
        List<WorkflowTaskInstanceDTO.ErrorReportDTO> list = baseMapper.errorReport(param);
        if (CollUtil.isEmpty(list)) {
            return list;
        }
        list.forEach(item -> {
            item.setSourceTypeName(WorkflowTaskRecordTypeEnum.getName(item.getSourceType()));
            item.setTargetServiceName(WorkflowTaskNodeConfigParser.resolveServiceName(item.getTargetService()));
        });
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTaskInstanceDTO.RetryResultDTO retry(WorkflowTaskInstanceDTO.RetryDTO dto) {
        WorkflowTaskRecordDTO.ForceRetryDTO forceRetryDTO = new WorkflowTaskRecordDTO.ForceRetryDTO();
        forceRetryDTO.setRetryCount(dto.getRetryCount());
        forceRetryDTO.setRemark(dto.getRemark());
        forceRetryDTO.setInstanceId(dto.getInstanceId());
        WorkflowTaskInstanceEntity instance = getById(dto.getInstanceId());
        if (instance == null) {
            throw new ServiceException(ApiError.WF_TASK_INSTANCE_NOT_FOUND);
        }
        forceRetryDTO.setSourceType(instance.getSourceType());
        forceRetryDTO.setSourceId(instance.getSourceId());
        WorkflowTaskRecordDTO.ForceRetryResultDTO result = workflowTaskRecordService.forceRetry(forceRetryDTO);
        WorkflowTaskInstanceDTO.RetryResultDTO wrapper = new WorkflowTaskInstanceDTO.RetryResultDTO();
        wrapper.setInstanceId(resolveInstanceId(dto.getInstanceId(), result));
        wrapper.setForceRetryResult(result);
        return wrapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowTaskInstanceDTO.RetryResultDTO retryFromStep(WorkflowTaskInstanceDTO.RetryFromStepDTO dto) {
        WorkflowTaskInstanceEntity instance = getById(dto.getInstanceId());
        if (instance == null) {
            throw new ServiceException(ApiError.WF_TASK_INSTANCE_NOT_FOUND);
        }
        return SpringUtil.getBean(WorkflowTaskInstanceServiceImpl.class)
                .retryFromStepWithLock(dto, instance.getSourceType(), instance.getSourceId());
    }

    /**
     * 带锁从指定 index 重跑：重置后续节点并在事务提交后发 MQ。
     * <p>权限由 Controller 层 @DataPermission(menuCode = "oms:workflowTaskInstance:retry") 统一校验。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.WORKFLOW_LOCK_KEY,
            keyName = "sourceType,sourceId", unlockAfterTx = true)
    public WorkflowTaskInstanceDTO.RetryResultDTO retryFromStepWithLock(WorkflowTaskInstanceDTO.RetryFromStepDTO dto,
                                                                        String sourceType, String sourceId) {
        WorkflowTaskInstanceEntity instance = getById(dto.getInstanceId());
        if (instance == null) {
            throw new ServiceException(ApiError.WF_TASK_INSTANCE_NOT_FOUND);
        }
        if (WorkflowTaskInstanceStatusEnum.CANCELLED.getCode().equals(instance.getStatus())
                || WorkflowTaskInstanceStatusEnum.SUCCESS.getCode().equals(instance.getStatus())) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NO_ELIGIBLE);
        }
        int fromIndex = Optional.ofNullable(dto.getFromIndex()).orElse(instance.getCurrentIndex());
        if (fromIndex < 0) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND);
        }
        List<WorkflowTaskRecordEntity> allSteps = workflowTaskRecordService.lambdaQuery()
                .eq(WorkflowTaskRecordEntity::getInstanceId, instance.getId())
                .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                .orderByAsc(WorkflowTaskRecordEntity::getIndex)
                .list();
        if (CollUtil.isEmpty(allSteps)) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND);
        }
        int maxIndex = allSteps.stream()
                .map(WorkflowTaskRecordEntity::getIndex)
                .max(Integer::compareTo)
                .orElse(0);
        if (fromIndex > maxIndex) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND);
        }
        List<WorkflowTaskRecordEntity> steps = allSteps.stream()
                .filter(step -> step.getIndex() != null && step.getIndex() >= fromIndex)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(steps)) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NOT_FOUND);
        }
        for (WorkflowTaskRecordEntity step : steps) {
            if (!workflowTaskRecordService.isStepForceRetryAllowed(step)) {
                throw new ServiceException(ApiError.WF_TASK_RECORD_FORCE_RETRY_NO_ELIGIBLE);
            }
        }
        Map<Integer, WorkflowTaskRecordEntity> indexTaskMap = allSteps.stream()
                .filter(step -> step.getIndex() != null)
                .collect(Collectors.toMap(WorkflowTaskRecordEntity::getIndex, step -> step, (a, b) -> a));
        for (WorkflowTaskRecordEntity step : steps) {
            String remark = workflowTaskRecordService.formatForceRetryRemark(step.getRemark(), dto.getRemark());
            String refreshedInputData = workflowTaskRecordService.getPreviousSuccessOutputData(step, indexTaskMap);
            Integer resetRetryCount = dto.getRetryCount() == null
                    ? Optional.ofNullable(step.getRetryCount()).orElse(0) + 1
                    : dto.getRetryCount();
            Integer currentVersion = Optional.ofNullable(step.getVersion()).orElse(0);
            boolean updated = workflowTaskRecordService.lambdaUpdate()
                    .eq(WorkflowTaskRecordEntity::getId, step.getId())
                    .eq(WorkflowTaskRecordEntity::getVersion, currentVersion)
                    .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.PENDING.getCode())
                    .set(WorkflowTaskRecordEntity::getRetryCount, resetRetryCount)
                    .set(WorkflowTaskRecordEntity::getLastError, "")
                    .set(WorkflowTaskRecordEntity::getRemark, remark)
                    .set(CharSequenceUtil.isNotBlank(refreshedInputData),
                            WorkflowTaskRecordEntity::getInputData, refreshedInputData)
                    .set(WorkflowTaskRecordEntity::getVersion, currentVersion + 1)
                    .update();
            if (!updated) {
                log.warn("retryFromStepWithLock 重置节点并发冲突，stepId={}, version={}", step.getId(), currentVersion);
                throw new ServiceException(ApiError.WF_TASK_INSTANCE_VERSION_CONFLICT);
            }
        }
        markRunningOrThrow(instance.getId(), fromIndex, instance.getTotalSteps());
        WorkflowTaskRecordDTO.AddTaskDTO dispatch = buildDispatchMessage(instance);
        dispatch.setTargetIndex(fromIndex);
        dispatch.setRetryFailedStep(Boolean.TRUE);
        dispatch.setForceRetry(Boolean.TRUE);
        registerDispatchAfterCommit(dispatch, instance.getSourceId());

        WorkflowTaskInstanceDTO.RetryResultDTO wrapper = new WorkflowTaskInstanceDTO.RetryResultDTO();
        wrapper.setInstanceId(instance.getId());
        WorkflowTaskRecordDTO.ForceRetryResultDTO forceRetryResult = new WorkflowTaskRecordDTO.ForceRetryResultDTO();
        forceRetryResult.setSourceType(instance.getSourceType());
        forceRetryResult.setSourceId(instance.getSourceId());
        forceRetryResult.setResetCount(steps.size());
        forceRetryResult.setScheduledMqCount(1);
        forceRetryResult.setMqCount(1);
        wrapper.setForceRetryResult(forceRetryResult);
        return wrapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(WorkflowTaskInstanceDTO.CancelDTO dto) {
        markCancelled(dto.getInstanceId(), dto.getRemark());
    }

    @Override
    public List<WorkflowTaskInstanceDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<WorkflowTaskInstanceDTO.TabListDTO> list = baseMapper.tabList(dto);
        List<WorkflowTaskInstanceDTO.TabListDTO> newList = new ArrayList<>();
        Arrays.stream(WorkflowTaskInstanceStatusEnum.values()).forEach(statusEnum -> {
            WorkflowTaskInstanceDTO.TabListDTO exists = list.stream().filter(item -> statusEnum.getCode().equals(item.getTabFlag())).findFirst().orElse(null);
            if (exists == null) {
                WorkflowTaskInstanceDTO.TabListDTO emptyItem = new WorkflowTaskInstanceDTO.TabListDTO();
                emptyItem.setTabFlag(statusEnum.getCode());
                emptyItem.setTabFlagName(statusEnum.getName());
                emptyItem.setCount(MathUtil.ZERO);
                newList.add(emptyItem);
            }else {
                exists.setTabFlagName(statusEnum.getName());
                newList.add(exists);
            }
        });
        return newList;
    }

    private String resolveInstanceId(String instanceId, WorkflowTaskRecordDTO.ForceRetryResultDTO result) {
        if (CharSequenceUtil.isNotBlank(instanceId)) {
            return instanceId;
        }
        if (result == null || CharSequenceUtil.hasBlank(result.getSourceType(), result.getSourceId())) {
            return null;
        }
        WorkflowTaskInstanceEntity instance = getLatestBySource(result.getSourceId(), result.getSourceType());
        return instance == null ? null : instance.getId();
    }

    /**
     * {@inheritDoc}
     * <p>仅用于 FAILED / WAITING 结果；SUCCESS 路径请使用 {@link #persistLastNodeSuccess} 或 {@link #persistMiddleNodeSuccess}。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persistNodeAndSyncInstance(WorkflowTaskRecordEntity node,
                                           String instanceId,
                                           int currentIndex,
                                           int totalSteps,
                                           StepInvokeResult.Outcome outcome,
                                           String lastError) {
        workflowTaskRecordService.updateById(node);
        if (StepInvokeResult.Outcome.WAITING.equals(outcome)) {
            markWaiting(instanceId, currentIndex, lastError);
        } else if (StepInvokeResult.Outcome.FAILED.equals(outcome)) {
            markFailed(instanceId, currentIndex, lastError);
        }
        // SUCCESS 路径由专用方法处理，此处不兜底，避免歧义
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persistLastNodeSuccess(WorkflowTaskRecordEntity node,
                                       String instanceId,
                                       int maxIndex,
                                       int totalSteps) {
        // 同一事务：节点 SUCCESS 落库 + 实例 SUCCESS 落库，消除两步写库之间的中间态
        workflowTaskRecordService.updateById(node);
        markSuccess(instanceId, maxIndex, totalSteps);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persistMiddleNodeSuccess(WorkflowTaskRecordEntity currentNode,
                                         WorkflowTaskRecordEntity nextNode,
                                         String nextInputData) {
        // 同一事务：当前节点 SUCCESS + 下一节点 inputData 回填
        workflowTaskRecordService.updateById(currentNode);
        if (nextNode != null && CharSequenceUtil.isNotBlank(nextInputData)) {
            nextNode.setInputData(nextInputData);
            workflowTaskRecordService.updateById(nextNode);
        }
    }

    /**
     * 事务提交后 MQ 发送失败时，新开事务将目标节点标记为 FAILED，确保 Job 可扫描到并补偿。
     * <p>由 registerDispatchAfterCommit 的 catch 块调用，使用独立事务避免与已提交事务干扰。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void markDispatchMqFailed(String instanceId, Integer targetIndex, String reason) {
        if (CharSequenceUtil.isBlank(instanceId) || targetIndex == null) {
            log.warn("markDispatchMqFailed 跳过：参数不完整，instanceId={}, targetIndex={}", instanceId, targetIndex);
            return;
        }
        WorkflowTaskRecordEntity node = workflowTaskRecordService.lambdaQuery()
                .eq(WorkflowTaskRecordEntity::getInstanceId, instanceId)
                .eq(WorkflowTaskRecordEntity::getIndex, targetIndex)
                .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                .last("LIMIT 1")
                .one();
        if (node != null && !WorkflowTaskRecordStatusEnum.SUCCESS.getCode().equals(node.getStatus())) {
            workflowTaskRecordService.lambdaUpdate()
                    .eq(WorkflowTaskRecordEntity::getId, node.getId())
                    .set(WorkflowTaskRecordEntity::getStatus, WorkflowTaskRecordStatusEnum.FAILED.getCode())
                    .set(WorkflowTaskRecordEntity::getLastError, CharSequenceUtil.blankToDefault(reason, "调度MQ发送失败"))
                    .set(WorkflowTaskRecordEntity::getEndTime, LocalDateTime.now())
                    .update();
        }
        markFailed(instanceId, targetIndex, CharSequenceUtil.blankToDefault(reason, "调度MQ发送失败"));
    }

    private WorkflowTaskRecordDTO.AddTaskDTO buildDispatchMessage(WorkflowTaskInstanceEntity instance) {
        WorkflowTaskRecordDTO.AddTaskDTO dto = new WorkflowTaskRecordDTO.AddTaskDTO();
        dto.setInstanceId(instance.getId());
        dto.setSourceId(instance.getSourceId());
        dto.setSourceCode(instance.getSourceCode());
        dto.setTraceId(instance.getTraceId());
        WorkflowTaskRecordTypeEnum taskRecordTypeEnum = WorkflowTaskRecordTypeEnum.getByCode(instance.getSourceType());
        if (Objects.isNull(taskRecordTypeEnum)) {
            throw new ServiceException(ApiError.WF_TASK_RECORD_TYPE_NOT_FOUND);
        }
        dto.setSourceTypeEnum(taskRecordTypeEnum);
        return dto;
    }

    private void fillListNames(WorkflowTaskInstanceDTO.ListDTO item) {
        item.setSourceTypeName(WorkflowTaskRecordTypeEnum.getName(item.getSourceType()));
        item.setStatusName(WorkflowTaskInstanceStatusEnum.getName(item.getStatus()));
        item.setAutoRetryExceeded(isAutoRetryExceeded(item.getStatus(), item.getRetryCount()));
    }

    private void fillViewNames(WorkflowTaskInstanceDTO.ViewDTO view) {
        view.setSourceTypeName(WorkflowTaskRecordTypeEnum.getName(view.getSourceType()));
        view.setStatusName(WorkflowTaskInstanceStatusEnum.getName(view.getStatus()));
    }

    private void fillAutoRetryExceeded(WorkflowTaskInstanceDTO.ViewDTO view, List<WorkflowTaskInstanceDTO.StepDTO> steps) {
        if (CollUtil.isEmpty(steps)) {
            return;
        }
        WorkflowTaskInstanceDTO.StepDTO current = steps.stream()
                .filter(s -> Objects.equals(s.getIndex(), view.getCurrentIndex()))
                .findFirst()
                .orElse(null);
        if (current != null) {
            view.setAutoRetryExceeded(isAutoRetryExceeded(view.getStatus(), current.getRetryCount()));
        }
    }

    private boolean isAutoRetryExceeded(String instanceStatus, Integer retryCount) {
        return WorkflowTaskInstanceStatusEnum.FAILED.getCode().equals(instanceStatus)
                && retryCount != null
                && retryCount > WorkflowTaskRecordService.AUTO_RETRY_MAX_COUNT;
    }

    private void fillStepNames(List<WorkflowTaskInstanceDTO.StepDTO> steps) {
        if (CollUtil.isEmpty(steps)) {
            return;
        }
        steps.sort(Comparator.comparing(WorkflowTaskInstanceDTO.StepDTO::getIndex));
        steps.forEach(step -> {
            step.setStatusName(WorkflowTaskRecordStatusEnum.getName(step.getStatus()));
            step.setTargetServiceName(WorkflowTaskNodeConfigParser.resolveServiceName(step.getTargetService()));
            step.setHandlerCode(step.getTargetEndpoint());
        });
    }

    /**
     * 事务提交后再发 MQ，避免消费端读不到未提交节点。
     * <p>MQ 发送失败时，新开独立事务将目标节点与实例标记为 FAILED，确保 Job 下次可扫到并补偿。</p>
     */
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
                        // 新开独立事务：回写节点 + 实例为 FAILED，让 Job 下次补偿重发
                        try {
                            SpringUtil.getBean(WorkflowTaskInstanceServiceImpl.class)
                                    .markDispatchMqFailed(
                                            dispatch.getInstanceId(),
                                            dispatch.getTargetIndex(),
                                            "调度MQ发送失败: " + ex.getMessage());
                        } catch (Exception markEx) {
                            log.error("MQ失败回写节点/实例状态异常，instanceId={}, targetIndex={}，需人工核查",
                                    dispatch.getInstanceId(), dispatch.getTargetIndex(), markEx);
                        }
                    }
                }
            });
            return;
        }
        workflowTaskStepDispatcher.sendDispatchMq(dispatch, messageKey);
    }
}
