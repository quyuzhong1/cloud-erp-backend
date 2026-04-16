package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageDispatchTaskEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageDispatchTaskService extends SuperService<MessageDispatchTaskEntity> {

    String createTask(MessageEntity messageEntity);

    String createUpgradePushTask(MessageEntity messageEntity);

    String refreshTask(MessageEntity messageEntity);

    void removeByMessageId(String messageId);

    List<String> listDueTaskIds(Integer limit);

    void executeTaskAsync(String taskId);

    void queueTask(String taskId, LocalDateTime executeTime);
}
