package com.erp.server.sys.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.NoticeDispatchDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageDispatchTaskEntity;
import com.erp.model.sys.enums.MessageDispatchTaskSceneEnum;
import com.erp.model.sys.enums.MessageDispatchTaskStatusEnum;
import com.erp.server.sys.mapper.MessageMapper;
import com.erp.server.sys.mapper.MessageDispatchTaskMapper;
import com.erp.server.sys.service.MessageDispatchTaskService;
import com.erp.server.sys.service.support.MessageDispatchDelayQueueSupport;
import com.erp.server.sys.service.support.NoticeClusterPublisher;
import com.erp.server.sys.service.support.NoticeSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageDispatchTaskServiceImpl extends SuperServiceImpl<MessageDispatchTaskMapper, MessageDispatchTaskEntity> implements MessageDispatchTaskService {

    private static final int MAX_RETRY_COUNT = 3;
    private static final int[] RETRY_MINUTES = {1, 5, 15};

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private NoticeClusterPublisher noticeClusterPublisher;

    @Resource
    private MessageDispatchDelayQueueSupport messageDispatchDelayQueueSupport;

    @Override
    public String createTask(MessageEntity messageEntity) {
        return saveTask(messageEntity, MessageDispatchTaskSceneEnum.PDA_NOTICE);
    }

    @Override
    public String createUpgradePushTask(MessageEntity messageEntity) {
        return saveTask(messageEntity, MessageDispatchTaskSceneEnum.PDA_UPGRADE_PUSH);
    }

    private String saveTask(MessageEntity messageEntity, MessageDispatchTaskSceneEnum sceneEnum) {
        MessageDispatchTaskEntity entity = new MessageDispatchTaskEntity();
        entity.setMessageId(messageEntity.getId());
        entity.setScene(sceneEnum.getCode());
        entity.setExecuteTime(NoticeSupport.resolveExecuteTime(messageEntity));
        entity.setStatus(MessageDispatchTaskStatusEnum.INIT.getCode());
        entity.setRetryCount(0);
        entity.setNextRetryTime(null);
        entity.setErrorMsg(null);
        if (!this.save(entity)) {
            throw new ServiceException("消息分发任务保存失败");
        }
        return entity.getId();
    }

    @Override
    public String refreshTask(MessageEntity messageEntity) {
        MessageDispatchTaskEntity taskEntity = this.lambdaQuery()
                .eq(MessageDispatchTaskEntity::getMessageId, messageEntity.getId())
                .eq(MessageDispatchTaskEntity::getScene, MessageDispatchTaskSceneEnum.PDA_NOTICE.getCode())
                .orderByDesc(MessageDispatchTaskEntity::getCreateTime)
                .last("LIMIT 1")
                .one();
        if (taskEntity == null || Objects.equals(MessageDispatchTaskStatusEnum.SUCCESS.getCode(), taskEntity.getStatus())) {
            return createTask(messageEntity);
        }
        LocalDateTime executeTime = NoticeSupport.resolveExecuteTime(messageEntity);
        this.lambdaUpdate()
                .eq(MessageDispatchTaskEntity::getId, taskEntity.getId())
                .set(MessageDispatchTaskEntity::getExecuteTime, executeTime)
                .set(MessageDispatchTaskEntity::getStatus, MessageDispatchTaskStatusEnum.INIT.getCode())
                .set(MessageDispatchTaskEntity::getRetryCount, 0)
                .set(MessageDispatchTaskEntity::getNextRetryTime, null)
                .set(MessageDispatchTaskEntity::getErrorMsg, null)
                .update();
        return taskEntity.getId();
    }

    @Override
    public void removeByMessageId(String messageId) {
        this.lambdaUpdate()
                .eq(MessageDispatchTaskEntity::getMessageId, messageId)
                .set(MessageDispatchTaskEntity::getIsDeleted, Boolean.TRUE)
                .update();
    }

    @Override
    public List<String> listDueTaskIds(Integer limit) {
        List<MessageDispatchTaskEntity> tasks = baseMapper.listDueTasks(MAX_RETRY_COUNT, limit);
        if (CollectionUtils.isEmpty(tasks)) {
            return new ArrayList<>();
        }
        return tasks.stream().map(MessageDispatchTaskEntity::getId).collect(Collectors.toList());
    }

    @Override
    @Async("thirdNoticePushExecutor")
    public void executeTaskAsync(String taskId) {
        MessageDispatchTaskEntity task = this.getById(taskId);
        if (task == null || !canExecute(task)) {
            return;
        }
        boolean locked = this.lambdaUpdate()
                .eq(MessageDispatchTaskEntity::getId, taskId)
                .in(MessageDispatchTaskEntity::getStatus, MessageDispatchTaskStatusEnum.INIT.getCode(), MessageDispatchTaskStatusEnum.FAILED.getCode())
                .set(MessageDispatchTaskEntity::getStatus, MessageDispatchTaskStatusEnum.RUNNING.getCode())
                .set(MessageDispatchTaskEntity::getErrorMsg, null)
                .update();
        if (!locked) {
            return;
        }

        try {
            processTask(taskId);
        } catch (Exception e) {
            log.error("Process message dispatch task failed, taskId={}", taskId, e);
            markFailed(taskId, e.getMessage());
        }
    }

    @Override
    public void queueTask(String taskId, LocalDateTime executeTime) {
        if (taskId == null) {
            return;
        }
        LocalDateTime finalExecuteTime = executeTime == null ? LocalDateTime.now() : executeTime;
        try {
            messageDispatchDelayQueueSupport.offer(taskId, finalExecuteTime);
            log.info("Queue message dispatch task success, taskId={}, executeTime={}", taskId, finalExecuteTime);
        } catch (Exception e) {
            log.error("Queue message dispatch task failed, taskId={}, executeTime={}", taskId, finalExecuteTime, e);
        }
    }

    private void processTask(String taskId) {
        MessageDispatchTaskEntity task = this.getById(taskId);
        if (task == null) {
            return;
        }
        MessageDispatchTaskSceneEnum sceneEnum = MessageDispatchTaskSceneEnum.getByCode(task.getScene());
        if (sceneEnum == null) {
            markSuccess(taskId);
            return;
        }
        MessageEntity messageEntity = messageMapper.selectById(task.getMessageId());
        if (messageEntity == null
                || Boolean.TRUE.equals(messageEntity.getIsDeleted())
                || (messageEntity.getExpireTime() != null && messageEntity.getExpireTime().isBefore(LocalDateTime.now()))) {
            markSuccess(taskId);
            return;
        }
        if (MessageDispatchTaskSceneEnum.PDA_NOTICE == sceneEnum) {
            processSystemNoticeTask(task, messageEntity);
            return;
        }
        if (MessageDispatchTaskSceneEnum.PDA_UPGRADE_PUSH == sceneEnum) {
            processUpgradePushTask(messageEntity);
            markSuccess(taskId);
            return;
        }
        markSuccess(taskId);
    }

    private void processSystemNoticeTask(MessageDispatchTaskEntity task, MessageEntity messageEntity) {
        if (!NoticeSupport.isRealtimeSystemNotice(messageEntity)) {
            markSuccess(task.getId());
            return;
        }
        log.info("Start process system notice dispatch task, taskId={}, messageId={}, scene={}, application={}, executeTime={}",
                task.getId(), task.getMessageId(), task.getScene(), messageEntity.getApplication(), task.getExecuteTime());
        NoticeDispatchDTO dispatchDTO = new NoticeDispatchDTO();
        dispatchDTO.setMessageId(task.getMessageId());
        dispatchDTO.setScene(task.getScene());
        dispatchDTO.setMarkReadOnSuccess(Boolean.TRUE);
        dispatchDTO.setNotice(NoticeSupport.buildSystemNotice(messageEntity));
        noticeClusterPublisher.publish(dispatchDTO);
        log.info("Finish process system notice dispatch task, taskId={}, messageId={}, scene={}, application={}",
                task.getId(), task.getMessageId(), task.getScene(), messageEntity.getApplication());
        markSuccess(task.getId());
    }

    private void processUpgradePushTask(MessageEntity messageEntity) {
        if (!NoticeSupport.isPdaUpgradeNotice(messageEntity)) {
            return;
        }
        log.info("Start process upgrade notice dispatch, messageId={}, application={}",
                messageEntity.getId(), messageEntity.getApplication());
        NoticeDispatchDTO dispatchDTO = new NoticeDispatchDTO();
        dispatchDTO.setMessageId(messageEntity.getId());
        dispatchDTO.setScene(MessageDispatchTaskSceneEnum.PDA_UPGRADE_PUSH.getCode());
        dispatchDTO.setMarkReadOnSuccess(Boolean.FALSE);
        dispatchDTO.setNotice(NoticeSupport.buildUpgradeNotice(messageEntity));
        noticeClusterPublisher.publish(dispatchDTO);
        log.info("Finish process upgrade notice dispatch, messageId={}, application={}",
                messageEntity.getId(), messageEntity.getApplication());
    }

    private boolean canExecute(MessageDispatchTaskEntity task) {
        if (task == null) {
            return false;
        }
        if (Objects.equals(MessageDispatchTaskStatusEnum.SUCCESS.getCode(), task.getStatus())
                || Objects.equals(MessageDispatchTaskStatusEnum.RUNNING.getCode(), task.getStatus())) {
            return false;
        }
        if (task.getExecuteTime() != null && task.getExecuteTime().isAfter(LocalDateTime.now())) {
            return false;
        }
        return task.getNextRetryTime() == null || !task.getNextRetryTime().isAfter(LocalDateTime.now());
    }

    private void markSuccess(String taskId) {
        this.lambdaUpdate()
                .eq(MessageDispatchTaskEntity::getId, taskId)
                .set(MessageDispatchTaskEntity::getStatus, MessageDispatchTaskStatusEnum.SUCCESS.getCode())
                .set(MessageDispatchTaskEntity::getErrorMsg, null)
                .set(MessageDispatchTaskEntity::getNextRetryTime, null)
                .update();
    }

    private void markFailed(String taskId, String errorMsg) {
        MessageDispatchTaskEntity task = this.getById(taskId);
        if (task == null) {
            return;
        }
        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
        retryCount++;

        LocalDateTime nextRetryTime = null;
        if (retryCount <= MAX_RETRY_COUNT) {
            nextRetryTime = LocalDateTime.now().plusMinutes(RETRY_MINUTES[Math.min(retryCount - 1, RETRY_MINUTES.length - 1)]);
        }

        this.lambdaUpdate()
                .eq(MessageDispatchTaskEntity::getId, taskId)
                .set(MessageDispatchTaskEntity::getStatus, MessageDispatchTaskStatusEnum.FAILED.getCode())
                .set(MessageDispatchTaskEntity::getRetryCount, retryCount)
                .set(MessageDispatchTaskEntity::getNextRetryTime, nextRetryTime)
                .set(MessageDispatchTaskEntity::getErrorMsg, errorMsg)
                .update();
    }
}
