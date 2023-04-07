package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 模板交付文档表
 * @TableName template_delivery_docs
 */
@Data
@TableName(value ="template_delivery_docs")
public class TemplateDeliveryDocsEntity extends BaseEntity implements Serializable {

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 文档名
     */
    private String docsName;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 是否是系统文档 1 是  0 不是
     */
    private Integer isSys;

    /**
     * 文档名id
     */
    private String docsNameId;

    private static final long serialVersionUID = 1L;
}