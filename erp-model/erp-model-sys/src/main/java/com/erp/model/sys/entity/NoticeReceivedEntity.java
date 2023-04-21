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
@TableName("notice_received")
public class NoticeReceivedEntity extends BaseEntity<NoticeReceivedEntity> {

    /**
     * 通知id
     */
    @TableField("notice_id")
    private String noticeId;

    /**
     * 接收者类型
     */
    @TableField("received_type")
    private String receivedType;

    /**
     * 接收的值
     */
    @TableField("received_vaule")
    private String receivedVaule;


    public static final String NOTICE_ID = "notice_id";

    public static final String RECEIVED_TYPE = "received_type";

    public static final String RECEIVED_VAULE = "received_vaule";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
