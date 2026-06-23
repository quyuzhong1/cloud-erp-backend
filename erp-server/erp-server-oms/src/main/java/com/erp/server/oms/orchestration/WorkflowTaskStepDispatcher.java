package com.erp.server.oms.orchestration;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskInstanceEntity;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.WorkflowTaskInstanceStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import com.erp.server.oms.service.WorkflowTaskInstanceService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 单步任务编排调度器：每条 MQ 只推进一个节点，成功后链式发送下一节点消息。
 */
@Slf4j
@Component
public class WorkflowTaskStepDispatcher {

    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;

    @Lazy
    @Resource
    private WorkflowTaskInstanceService workflowTaskInstanceService;

    @Resource
    private CrossServiceStepInvoker crossServiceStepInvoker;

    @Resource
    private MQProducerService mqProducerService;

    /**
     * 消费 MQ 后的单步调度入口：解析实例、抢占节点、跨服务执行，并按结果推进或终止链路。
     *
     * @param mqDTO 调度消息体，需包含 sourceType/sourceId；可选 instanceId、targetIndex、retryFailedStep、forceRetry
     */
    public void dispatch(WorkflowTaskRecordDTO.AddTaskDTO mqDTO) {
        if (mqDTO == null || CharSequenceUtil.isBlank(mqDTO.getSourceId()) || mqDTO.getSourceTypeEnum() == null) {
            log.error("WorkflowTaskStepDispatcher 参数为空");
            return;
        }

        WorkflowTaskInstanceEntity instance = resolveInstance(mqDTO);
        if (instance == null) {
            log.error("未找到编排实例，sourceType={}, sourceId={}", mqDTO.getSourceTypeEnum().getCode(), mqDTO.getSourceId());
            return;
        }

        if (WorkflowTaskInstanceStatusEnum.SUCCESS.getCode().equals(instance.getStatus())
                || WorkflowTaskInstanceStatusEnum.CANCELLED.getCode().equals(instance.getStatus())) {
            log.info("编排实例已终态，跳过调度，instanceId={}, status={}", instance.getId(), instance.getStatus());
            return;
        }

        if (WorkflowTaskInstanceStatusEnum.FAILED.getCode().equals(instance.getStatus())
                && !Boolean.TRUE.equals(mqDTO.getRetryFailedStep())) {
            log.warn("编排实例已失败，等待 Job/人工重试，instanceId={}", instance.getId());
            return;
        }

        List<WorkflowTaskRecordEntity> steps = listSteps(instance);
        if (CollUtil.isEmpty(steps)) {
            log.error("编排实例无节点，instanceId={}", instance.getId());
            return;
        }

        Map<Integer, WorkflowTaskRecordEntity> indexMap = steps.stream()
                .collect(Collectors.toMap(WorkflowTaskRecordEntity::getIndex, e -> e, (a, b) -> a));

        Integer targetIndex = resolveTargetIndex(mqDTO, steps, indexMap);
        if (targetIndex == null) {
            markInstanceSuccess(instance, steps);
            return;
        }

        WorkflowTaskRecordEntity current = indexMap.get(targetIndex);
        if (current == null) {
            log.error("任务节点缺失，index={}, instanceId={}", targetIndex, instance.getId());
            return;
        }

        if (WorkflowTaskRecordStatusEnum.SUCCESS.getCode().equals(current.getStatus())) {
            scheduleNextStep(instance, mqDTO, targetIndex, indexMap);
            return;
        }

        if (WorkflowTaskRecordStatusEnum.FAILED.getCode().equals(current.getStatus())
                && !Boolean.TRUE.equals(mqDTO.getRetryFailedStep())) {
            syncInstanceFailed(instance, current);
            log.warn("失败节点等待 Job/人工重试，index={}, instanceId={}", targetIndex, instance.getId());
            return;
        }

        if (WorkflowTaskRecordStatusEnum.FAILED.getCode().equals(current.getStatus())
                && Boolean.TRUE.equals(mqDTO.getRetryFailedStep())
                && Optional.ofNullable(current.getRetryCount()).orElse(0) > WorkflowTaskRecordService.AUTO_RETRY_MAX_COUNT
                && !Boolean.TRUE.equals(mqDTO.getForceRetry())) {
            syncInstanceFailed(instance, current);
            log.warn("节点已达自动重试上限，需人工 forceRetry，index={}, retryCount={}, instanceId={}",
                    targetIndex, current.getRetryCount(), instance.getId());
            return;
        }

        // 先按重置前状态判断是否计重试，超时 PROCESSING 回拨为 PENDING 后仍应消耗一次自动重试次数。
        boolean retryIncrement = WorkflowTaskRecordStatusEnum.FAILED.getCode().equals(current.getStatus())
                || WorkflowTaskRecordStatusEnum.PROCESSING.getCode().equals(current.getStatus());

        if (WorkflowTaskRecordStatusEnum.PROCESSING.getCode().equals(current.getStatus())) {
            if (isProcessingWithinTimeout(current.getUpdateTime())) {
                log.warn("节点处理中且未超时，index={}, instanceId={}", targetIndex, instance.getId());
                return;
            }
            log.warn("节点处理超时，重置后重试，index={}, instanceId={}", targetIndex, instance.getId());
            if (Boolean.TRUE.equals(workflowTaskRecordService.resetStaleProcessingTask(current.getId()))) {
                current.setStatus(WorkflowTaskRecordStatusEnum.PENDING.getCode());
            }
        }

        workflowTaskInstanceService.markRunning(instance.getId(), targetIndex, steps.size());

        StepInvokeResult result = crossServiceStepInvoker.invoke(current, null, retryIncrement);
        if (result.getOutcome() == StepInvokeResult.Outcome.NOT_CLAIMED
                || result.getOutcome() == StepInvokeResult.Outcome.SKIPPED) {
            return;
        }

        if (!isInstanceActive(instance.getId())) {
            log.info("编排实例已取消或终态，终止后续调度，instanceId={}", instance.getId());
            return;
        }

        if (result.isWaiting()) {
            workflowTaskInstanceService.markWaiting(instance.getId(), targetIndex, result.getErrorMsg());
            return;
        }

        if (result.isFailed()) {
            workflowTaskInstanceService.markFailed(instance.getId(), targetIndex, result.getErrorMsg());
            return;
        }

        if (result.isSuccess()) {
            WorkflowTaskRecordEntity next = indexMap.get(targetIndex + 1);
            if (next != null && CharSequenceUtil.isNotBlank(result.getOutputDataJson())) {
                next.setInputData(result.getOutputDataJson());
                workflowTaskRecordService.updateById(next);
            }
            if (next == null) {
                markInstanceSuccess(instance, steps);
                return;
            }
            scheduleNextStep(instance, mqDTO, targetIndex, indexMap);
        }
    }

    /**
     * 当前节点成功后，发送 index+1 的单步 MQ 并将实例标记为 running。
     *
     * @param instance       编排实例
     * @param template       原始调度消息模板（继承 traceId 等字段）
     * @param completedIndex 刚执行完成的节点序号
     * @param indexMap       实例下全部节点 index → entity 映射
     */
    public void scheduleNextStep(WorkflowTaskInstanceEntity instance, WorkflowTaskRecordDTO.AddTaskDTO template,
                                 int completedIndex, Map<Integer, WorkflowTaskRecordEntity> indexMap) {
        if (!isInstanceActive(instance.getId())) {
            log.info("编排实例已取消或终态，跳过链式调度，instanceId={}", instance.getId());
            return;
        }
        int nextIndex = completedIndex + 1;
        if (!indexMap.containsKey(nextIndex)) {
            markInstanceSuccess(instance, indexMap.values().stream()
                    .sorted(Comparator.comparing(WorkflowTaskRecordEntity::getIndex))
                    .collect(Collectors.toList()));
            return;
        }
        WorkflowTaskRecordDTO.AddTaskDTO nextMsg = copyDispatchMessage(template, instance);
        nextMsg.setTargetIndex(nextIndex);
        nextMsg.setRetryFailedStep(Boolean.FALSE);
        sendDispatchMq(nextMsg, instance.getSourceId());
        workflowTaskInstanceService.markRunning(instance.getId(), nextIndex, indexMap.size());
    }

    /**
     * 发送任务编排单步调度 MQ；发送失败时抛出 {@link com.common.core.enums.ApiError#WF_TASK_RECORD_MQ_SEND_FAILED}。
     *
     * @param addTaskDTO  调度载荷
     * @param messageKey  RocketMQ MessageKey，通常使用 sourceId
     */
    public void sendDispatchMq(WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO, String messageKey) {
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(
                RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC,
                RocketMqTagEnum.OMS_WORKFLOW_TASK_RECORD_TAG.getName(),
                addTaskDTO,
                messageKey,
                1);
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())) {
            log.error("任务编排调度 MQ 发送失败，payload={}, result={}", JSONUtil.toJsonStr(addTaskDTO), JSONUtil.toJsonStr(result));
            throw new ServiceException(ApiError.WF_TASK_RECORD_MQ_SEND_FAILED, JSONUtil.toJsonStr(result));
        }
    }

    private WorkflowTaskInstanceEntity resolveInstance(WorkflowTaskRecordDTO.AddTaskDTO mqDTO) {
        if (CharSequenceUtil.isNotBlank(mqDTO.getInstanceId())) {
            WorkflowTaskInstanceEntity instance = workflowTaskInstanceService.getById(mqDTO.getInstanceId());
            if (instance != null && !Boolean.TRUE.equals(instance.getIsDeleted())) {
                return instance;
            }
            log.error("编排实例不存在或已删除，instanceId={}", mqDTO.getInstanceId());
            return null;
        }
        WorkflowTaskInstanceEntity instance = workflowTaskInstanceService.getLatestBySource(
                mqDTO.getSourceId(), mqDTO.getSourceTypeEnum().getCode());
        if (instance != null) {
            return instance;
        }
        List<WorkflowTaskRecordEntity> legacySteps = workflowTaskRecordService.listLegacyBySourceId(
                mqDTO.getSourceId(), mqDTO.getSourceTypeEnum().getCode());
        if (CollUtil.isEmpty(legacySteps)) {
            return null;
        }
        return workflowTaskInstanceService.ensureInstanceForLegacy(mqDTO, legacySteps);
    }

    private List<WorkflowTaskRecordEntity> listSteps(WorkflowTaskInstanceEntity instance) {
        if (CharSequenceUtil.isNotBlank(instance.getId())) {
            List<WorkflowTaskRecordEntity> byInstance = workflowTaskRecordService.lambdaQuery()
                    .eq(WorkflowTaskRecordEntity::getInstanceId, instance.getId())
                    .eq(WorkflowTaskRecordEntity::getIsDeleted, false)
                    .orderByAsc(WorkflowTaskRecordEntity::getIndex)
                    .list();
            if (CollUtil.isNotEmpty(byInstance)) {
                return byInstance;
            }
        }
        return workflowTaskRecordService.listLegacyBySourceId(instance.getSourceId(), instance.getSourceType());
    }

    private Integer resolveTargetIndex(WorkflowTaskRecordDTO.AddTaskDTO mqDTO,
                                       List<WorkflowTaskRecordEntity> steps,
                                       Map<Integer, WorkflowTaskRecordEntity> indexMap) {
        if (mqDTO.getTargetIndex() != null && indexMap.containsKey(mqDTO.getTargetIndex())) {
            return mqDTO.getTargetIndex();
        }
        Optional<WorkflowTaskRecordEntity> firstPending = steps.stream()
                .filter(e -> !WorkflowTaskRecordStatusEnum.SUCCESS.getCode().equals(e.getStatus()))
                .min(Comparator.comparing(WorkflowTaskRecordEntity::getIndex));
        return firstPending.map(WorkflowTaskRecordEntity::getIndex).orElse(null);
    }

    private void markInstanceSuccess(WorkflowTaskInstanceEntity instance, List<WorkflowTaskRecordEntity> steps) {
        int maxIndex = steps.stream().map(WorkflowTaskRecordEntity::getIndex).max(Integer::compareTo).orElse(0);
        workflowTaskInstanceService.markSuccess(instance.getId(), maxIndex, steps.size());
    }

    private void syncInstanceFailed(WorkflowTaskInstanceEntity instance, WorkflowTaskRecordEntity step) {
        workflowTaskInstanceService.markFailed(instance.getId(), step.getIndex(), step.getLastError());
    }

    private WorkflowTaskRecordDTO.AddTaskDTO copyDispatchMessage(WorkflowTaskRecordDTO.AddTaskDTO template,
                                                                 WorkflowTaskInstanceEntity instance) {
        WorkflowTaskRecordDTO.AddTaskDTO dto = new WorkflowTaskRecordDTO.AddTaskDTO();
        dto.setInstanceId(instance.getId());
        dto.setSourceId(instance.getSourceId());
        dto.setSourceCode(instance.getSourceCode());
        dto.setTraceId(CharSequenceUtil.blankToDefault(template.getTraceId(), instance.getTraceId()));
        dto.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.getByCode(instance.getSourceType()));
        dto.setDictBasicTypeEnum(template.getDictBasicTypeEnum());
        return dto;
    }

    private boolean isProcessingWithinTimeout(LocalDateTime updateTime) {
        if (Objects.isNull(updateTime)) {
            return false;
        }
        return updateTime.plusMinutes(WorkflowTaskRecordService.TASK_PROCESSING_TIMEOUT_MINUTES).isAfter(LocalDateTime.now());
    }

    private boolean isInstanceActive(String instanceId) {
        WorkflowTaskInstanceEntity fresh = workflowTaskInstanceService.getById(instanceId);
        if (fresh == null || Boolean.TRUE.equals(fresh.getIsDeleted())) {
            return false;
        }
        return !WorkflowTaskInstanceStatusEnum.SUCCESS.getCode().equals(fresh.getStatus())
                && !WorkflowTaskInstanceStatusEnum.CANCELLED.getCode().equals(fresh.getStatus());
    }
}
