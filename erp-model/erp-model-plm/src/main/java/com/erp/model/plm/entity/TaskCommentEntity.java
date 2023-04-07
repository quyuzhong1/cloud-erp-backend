package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.common.core.entity.BaseEntity;
import lombok.Data;

/**
 * 任务评论表
 * @TableName task_comment
 */
@TableName(value ="task_comment")
@Data
public class TaskCommentEntity extends BaseEntity implements Serializable {

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}