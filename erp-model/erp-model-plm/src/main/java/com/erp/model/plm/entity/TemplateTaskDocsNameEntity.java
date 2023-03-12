package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 模板任务文档名称表
 *
 * @TableName template_task_docs_name
 */
@Data
@TableName(value = "template_task_docs_name")
public class TemplateTaskDocsNameEntity implements Serializable {
    /**
     *
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 文件名
     */
    private String name;

    /**
     * 模板id
     */
    private String templateId;


    /**
     * 启用状态 true 启用
     */
    private Boolean status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(value="create_user_name",fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建人id
     */
    @TableField(value="create_user_id",fill = FieldFill.INSERT)
    private String createUserId;


    /**
     * 更新人
     */
    @TableField(value ="update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 更新人id
     */
    @TableField(value ="update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;


}