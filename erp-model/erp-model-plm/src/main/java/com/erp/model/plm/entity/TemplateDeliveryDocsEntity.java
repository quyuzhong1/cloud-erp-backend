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
     * id
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
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private String createUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 更新人
     */
    @TableField("update_user_name")
    private String updateUserName;

    /**
     * 更新人id
     */
    @TableField("update_user_id")
    private String updateUserId;

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

    /**
     * 辅助字段：模板状态(1启用，0禁用)
     */
    private Integer status;

    private static final long serialVersionUID = 1L;


}