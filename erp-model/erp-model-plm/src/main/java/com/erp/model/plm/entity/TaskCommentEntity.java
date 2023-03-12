package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 任务评论表
 * @TableName task_comment
 */
@TableName(value ="task_comment")
@Data
public class TaskCommentEntity implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 
     */
    private String taskId;

    /**
     * 父级id
     */
    private String pid;

    /**
     * 内容
     */
    private String comment;

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
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名
     */
    private String createUserName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}