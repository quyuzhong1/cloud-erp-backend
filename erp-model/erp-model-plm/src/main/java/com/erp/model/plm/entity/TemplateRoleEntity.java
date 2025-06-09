package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 项目角色表
 * @TableName template_role
 */
@Data
@TableName(value ="template_role")
public class TemplateRoleEntity extends BaseEntity<TemplateRoleEntity> implements Serializable {

    /**
     * 角色名
     */
    private String name;

    /**
     * 标示id
     */
    private String templateId;

    private static final long serialVersionUID = 1L;

}