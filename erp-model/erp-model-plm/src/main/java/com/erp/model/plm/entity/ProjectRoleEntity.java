package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname ProjectRoleEntity
 * @Description TODO
 * @Date 2022-10-10 10:09
 * @Created by yl
 */
@TableName(value ="project_role")
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectRoleEntity extends BaseEntity implements Serializable {

    /**
     * 角色名
     */
    private String name;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 项目id
     */
    private String projectId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
