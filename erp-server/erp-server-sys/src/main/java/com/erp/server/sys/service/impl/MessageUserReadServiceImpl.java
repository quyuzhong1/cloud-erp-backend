package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.server.sys.mapper.MessageUserReadMapper;
import com.erp.server.sys.service.MessageUserReadService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 消息通知用户读取表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
 */
@Slf4j
@Service
public class MessageUserReadServiceImpl extends SuperServiceImpl<MessageUserReadMapper, MessageUserReadEntity> implements MessageUserReadService {

    @Override
    public List<MessageUserReadEntity> listByUserId(String userId) {
        return lambdaQuery()
                .eq(MessageUserReadEntity::getUserId, userId)
                .eq(MessageUserReadEntity::getIsRead, Boolean.TRUE)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(MessageUserReadEntity messageUserReadEntity) {
        return this.save(messageUserReadEntity);
    }

    @Override
    public MessageUserReadEntity listByMessageId(String messageId) {
        return lambdaQuery().eq(MessageUserReadEntity::getMessageId, messageId).last("LIMIT 1").one();
    }

    @Override
    public Boolean readByMessageId(String messageId, String userId) {
        return markReadByUserIds(messageId, java.util.Collections.singleton(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markReadByUserIds(String messageId, Collection<String> userIds) {
        if (CollectionUtils.isEmpty(userIds) || messageId == null) {
            return Boolean.TRUE;
        }
        List<String> distinctUserIds = userIds.stream().filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(distinctUserIds)) {
            return Boolean.TRUE;
        }
        List<MessageUserReadEntity> existingList = lambdaQuery()
                .eq(MessageUserReadEntity::getMessageId, messageId)
                .in(MessageUserReadEntity::getUserId, distinctUserIds)
                .list();
        Set<String> existingUserIds = existingList.stream().map(MessageUserReadEntity::getUserId).collect(Collectors.toSet());
        if (!existingUserIds.isEmpty()) {
            lambdaUpdate()
                    .set(MessageUserReadEntity::getIsRead, Boolean.TRUE)
                    .eq(MessageUserReadEntity::getMessageId, messageId)
                    .in(MessageUserReadEntity::getUserId, existingUserIds)
                    .update();
        }
        List<MessageUserReadEntity> insertList = new ArrayList<>();
        for (String userId : distinctUserIds) {
            if (existingUserIds.contains(userId)) {
                continue;
            }
            MessageUserReadEntity entity = new MessageUserReadEntity();
            entity.setMessageId(messageId);
            entity.setUserId(userId);
            entity.setIsRead(Boolean.TRUE);
            insertList.add(entity);
        }
        saveReadRecords(insertList);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markReadByMessageIds(Collection<String> messageIds, String userId) {
        if (CollectionUtils.isEmpty(messageIds) || userId == null) {
            return Boolean.TRUE;
        }
        List<String> distinctMessageIds = messageIds.stream().filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(distinctMessageIds)) {
            return Boolean.TRUE;
        }
        List<MessageUserReadEntity> existingList = lambdaQuery()
                .eq(MessageUserReadEntity::getUserId, userId)
                .in(MessageUserReadEntity::getMessageId, distinctMessageIds)
                .list();
        Set<String> existingMessageIds = existingList.stream().map(MessageUserReadEntity::getMessageId).collect(Collectors.toSet());
        if (!existingMessageIds.isEmpty()) {
            lambdaUpdate()
                    .set(MessageUserReadEntity::getIsRead, Boolean.TRUE)
                    .eq(MessageUserReadEntity::getUserId, userId)
                    .in(MessageUserReadEntity::getMessageId, existingMessageIds)
                    .update();
        }
        List<MessageUserReadEntity> insertList = new ArrayList<>();
        for (String messageId : distinctMessageIds) {
            if (existingMessageIds.contains(messageId)) {
                continue;
            }
            MessageUserReadEntity entity = new MessageUserReadEntity();
            entity.setMessageId(messageId);
            entity.setUserId(userId);
            entity.setIsRead(Boolean.TRUE);
            insertList.add(entity);
        }
        saveReadRecords(insertList);
        return Boolean.TRUE;
    }

    @Override
    public Boolean removeByMessageId(String messageId) {
        LambdaUpdateWrapper<MessageUserReadEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MessageUserReadEntity::getMessageId, messageId);
        return remove(wrapper);
    }

    private void saveReadRecords(List<MessageUserReadEntity> insertList) {
        if (CollectionUtils.isEmpty(insertList)) {
            return;
        }
        try {
            this.saveBatch(insertList, 500);
        } catch (Exception e) {
            log.warn("Save message read records duplicated, fallback to update. size={}", insertList.size(), e);
            Set<String> messageIds = insertList.stream().map(MessageUserReadEntity::getMessageId).collect(Collectors.toSet());
            Set<String> userIds = insertList.stream().map(MessageUserReadEntity::getUserId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(messageIds) || CollectionUtils.isEmpty(userIds)) {
                return;
            }
            lambdaUpdate()
                    .set(MessageUserReadEntity::getIsRead, Boolean.TRUE)
                    .in(MessageUserReadEntity::getMessageId, messageIds)
                    .in(MessageUserReadEntity::getUserId, userIds)
                    .update();
        }
    }
}
