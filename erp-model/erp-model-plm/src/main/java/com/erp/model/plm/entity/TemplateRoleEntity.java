package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目角色表
 * @TableName template_role
 */
@Data
@TableName(value ="template_role")
public class TemplateRoleEntity implements Serializable {
    /**
     * id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 角色名
     */
    private String name;

    /**
     * 
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人id
     */
    @TableField("upcrteate_user_id")
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField("crteate_user_name")
    private String createUserName;

    /**
     * 
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 更新人id
     */
    @TableField("update_user_id")
    private String updateUserId;

    /**
     * 更新人
     */
    @TableField("update_user_name")
    private String updateUserName;

    /**
     * 标示id
     */
    private String templateId;

    private static final long serialVersionUID = 1L;


}