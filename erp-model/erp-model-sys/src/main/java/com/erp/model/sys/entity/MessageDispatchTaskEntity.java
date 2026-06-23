package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("message_dispatch_task")
public class MessageDispatchTaskEntity extends BaseEntity<MessageDispatchTaskEntity> {

    @TableField("message_id")
    private String messageId;

    @TableField("scene")
    private String scene;

    @TableField("execute_time")
    private LocalDateTime executeTime;

    @TableField("status")
    private String status;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("next_retry_time")
    private LocalDateTime nextRetryTime;

    @TableField("error_msg")
    private String errorMsg;

    @Override
    public Serializable pkVal() {
        return this.getId();
    }
}
