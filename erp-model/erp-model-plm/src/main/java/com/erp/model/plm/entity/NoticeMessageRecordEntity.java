package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消息通知记录表
 * @TableName notice_message_record
 */
@TableName(value ="notice_message_log")
@Data
@NoArgsConstructor
public class NoticeMessageRecordEntity implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 任务名
     */
    private String taskName;

    /**
     * 产品名
     */
    private String productName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 通知的节点
     */
    private String noticeNode;

    /**
     * 通知表id
     */
    private String noticeMessageId;

    /**
     *预计结束时间
     */
    private LocalDate planEndTime;
    /**
     * 通知的用户id
     */
    private String noticeUserId;


    /**
     *负责人id
     */
    private String chargeId;

    /**
     *负责人名
     */
    private String chargeName;


    /**
     *是不是任务通知
     * 1 是 就是表示 是任务负责人
     * 0 不是 就是产品经理
     */
    private Integer isTask;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public NoticeMessageRecordEntity(String titleContent, String id, String flag, String userId, String productId, String name, String chargeName) {
        this.messageContent = titleContent;
        this.noticeMessageId = id;
        this.noticeNode = flag;
        this.noticeUserId = userId;
        this.chargeName = chargeName;
    }
}