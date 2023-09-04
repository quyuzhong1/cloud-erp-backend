package com.erp.server.sys.service.impl;

import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.server.sys.mapper.MessageUserReadMapper;
import com.erp.server.sys.service.MessageUserReadService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
    public Boolean readByMessageId(String messageId) {
        return lambdaUpdate()
                .set(MessageUserReadEntity::getIsRead, Boolean.TRUE)
                .eq(MessageUserReadEntity::getMessageId, messageId)
                .update();
    }
}
