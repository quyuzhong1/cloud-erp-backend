package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName role_ref_members
 */
@TableName(value ="role_ref_members")
@Data
@Accessors(chain = true)
public class RoleRefMemberEntity extends BaseEntity<RoleRefMemberEntity> implements Serializable {

    /**
     * 成员表id
     */
    private String membersId;

    /**
     * 项目角色表id
     */
    private String roleId;

    private String productId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}