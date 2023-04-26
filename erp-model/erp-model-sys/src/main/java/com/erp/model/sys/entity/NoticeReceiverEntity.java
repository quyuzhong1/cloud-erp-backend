package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 通知接收人信息
 * </p>
 *
 * @author lambda
 * @since 2023-04-20
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("notice_receiver")
public class NoticeReceiverEntity extends BaseEntity<NoticeReceiverEntity> {

    /**
     * 通知id
     */
    @TableField("notice_id")
    private String noticeId;

    /**
     * 接收者类型
     */
    @TableField("receiver_type")
    private String receiverType;

    /**
     * 接收的值
     */
    @TableField("receiver_value")
    private String receiverValue;

    @TableField("receiver_type_name")
    private String receiverTypeName;


    @Override
    public Serializable pkVal() {
        return null;
    }

}
