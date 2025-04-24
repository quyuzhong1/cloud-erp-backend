package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName docs_permission
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("template_docs_permission")
public class TemplateDocsPermissionEntity extends BaseEntity<TemplateDocsPermissionEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交付文档id
     */
    private String deliveryDocsId;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 查看的角色id
     */
    private String queryRoleId;

}