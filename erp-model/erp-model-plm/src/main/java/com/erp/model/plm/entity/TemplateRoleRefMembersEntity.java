package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 角色用户关系表
 * @TableName template_role_ref_members
 */
@Data
@TableName(value ="template_role_ref_members")
public class TemplateRoleRefMembersEntity extends BaseEntity<TemplateRoleRefMembersEntity> implements Serializable {
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

    private static final long serialVersionUID = 1L;
}