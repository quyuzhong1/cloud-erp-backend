package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
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
public class TemplateTaskDocsNameEntity extends BaseEntity<TemplateTaskDocsNameEntity> implements Serializable {

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

}