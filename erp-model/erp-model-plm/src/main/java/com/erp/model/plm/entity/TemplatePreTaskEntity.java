package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务的前置任务表
 * @TableName template_pre_task
 */
@Data
@TableName(value ="template_pre_task")
public class TemplatePreTaskEntity implements Serializable {
    /**
     * 
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 前置任务id
     */
    private String preTaskId;

    /**
     * 
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)

    private Date updateTime;

    /**
     * 模板id
     */
    private String templateId;

    private static final long serialVersionUID = 1L;


}