package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色用户关系表
 * @TableName template_role_ref_members
 */
@Data
@TableName(value ="template_role_ref_members")
public class TemplateRoleRefMembersEntity implements Serializable {
    /**
     * 
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 成员表id
     */
    private String membersId;

    /**
     * 项目角色表id
     */
    private String roleId;

    /**
     * 模板id
     */
    private String templateId;

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
     * 更新人id
     */
    @TableField("update_user_id")
    private String updateUserId;

    /**
     * 更新人
     */
    @TableField("update_user_name")
    private String updateUserName;

    private static final long serialVersionUID = 1L;


}