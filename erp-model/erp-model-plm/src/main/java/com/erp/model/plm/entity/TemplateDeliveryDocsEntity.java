package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 模板交付文档表
 * @TableName template_delivery_docs
 */
@Data
@TableName(value ="template_delivery_docs")
public class TemplateDeliveryDocsEntity implements Serializable {
    /**
     * 
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 文档名
     */
    private String docsName;

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
     * 任务id
     */
    private String taskId;

    /**
     * 是否是系统文档 1 是  0 不是
     */
    private Short isSys;

    /**
     * 文档名id
     */
    private String docsNameId;

    private static final long serialVersionUID = 1L;


}