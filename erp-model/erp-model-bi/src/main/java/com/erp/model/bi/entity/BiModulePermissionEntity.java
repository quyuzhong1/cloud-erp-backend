package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 模块权限表(BiModulePermission)实体类
 *
 * @author yl
 * @since 2022-12-12 10:28:35
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_module_permission")
public class BiModulePermissionEntity implements Serializable {
    private static final long serialVersionUID = -95417716101777218L;


    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 模块表id
     */
    private String moduleId;
    /**
     * 用户Id
     */
    private String userId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDate createTime;

    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDate updateTime;



}

