package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 消息通知用户读取表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("message_user_read")
public class MessageUserReadEntity extends BaseEntity<MessageUserReadEntity> {


    /**
    * 审核状态
    */
    @TableField("approve_status")
    private String approveStatus;

    /**
    * 消息通知表id
    */
    @TableField("message_id")
    private String messageId;

    /**
    * 读取的用户id
    */
    @TableField("user_id")
    private String userId;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String MESSAGE_ID = "message_id";

    public static final String USER_ID = "user_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}