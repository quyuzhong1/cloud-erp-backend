package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
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
public class ProjectRoleEntity implements Serializable {

    /**
     *角色id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 角色名
     */
    private String name;

    /**
     *
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     *
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 标示id
     */
    private String flagId;


    /**
     * 项目id
     */
    private String projectId;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
