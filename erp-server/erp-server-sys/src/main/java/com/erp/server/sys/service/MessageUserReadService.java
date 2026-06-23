package com.erp.server.sys.service;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.common.business.service.SuperService;

import java.util.Collection;
import java.util.List;


/**
 * <p>
 * 消息通知用户读取表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
 */
public interface MessageUserReadService extends SuperService<MessageUserReadEntity> {

    /**
     * 根据用户id查询读取的消息
     * @Author Luo_WG
     * @Date 2023/8/10 15:46
     * @param userId
     * @return java.util.List<com.erp.model.sys.entity.MessageUserReadEntity>
     **/
    List<MessageUserReadEntity> listByUserId(String userId);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/8/11 16:23
     * @param messageUserReadEntity
     * @return java.lang.Boolean
     **/
     Boolean add(MessageUserReadEntity messageUserReadEntity);


    /**
     * 根据消息id查询读取的消息
     * @Author Luo_WG
     * @Date 2023/8/10 15:46
     * @param messageId
     * @return java.util.List<com.erp.model.sys.entity.MessageUserReadEntity>
     **/
    MessageUserReadEntity listByMessageId(String messageId);

    /**
     * 根据消息id读取消息
     * @Author Luo_WG
     * @Date 2023/8/21 11:41
     * @param messageId
     * @param userId
     * @return java.lang.Boolean
     **/
    Boolean readByMessageId(String messageId, String userId);

    /**
     * 批量标记同一消息的多个用户为已读
     */
    Boolean markReadByUserIds(String messageId, Collection<String> userIds);

    /**
     * 批量标记同一用户的多条消息为已读
     */
    Boolean markReadByMessageIds(Collection<String> messageIds, String userId);

    /**
     * 根据消息id删除读取记录
     */
    Boolean removeByMessageId(String messageId);
}
