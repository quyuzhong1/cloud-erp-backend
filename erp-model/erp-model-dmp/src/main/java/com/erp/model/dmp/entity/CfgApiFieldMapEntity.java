package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * @description:API字段映射表
 * @author Will
 * @date: 2023/1/11 11:23
 */
@Data
@TableName(value ="cfg_api_field_map")
public class CfgApiFieldMapEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 平台名称
     */
    @TableField(value = "api_platform")
    private String apiPlatform;

    /**
     * 平台id
     */
    @TableField(value = "api_platform_id")
    private String apiPlatformId;

    /**
     * 模块类型 （0产品信息，1BOM管理）
     */
    @TableField(value = "module_type")
    private Integer moduleType;

    /**
     * 取值方式 (0 直接复制，1 按对照表赋值）
     */
    @TableField(value = "field_type")
    private Integer fieldType;

    /**
     * 本系统的字段
     */
    @TableField(value = "self_field")
    private String selfField;

    /**
     * 本系统的字段中文描述
     */
    @TableField(value = "self_field_name")
    private String selfFieldName;

    /**
     * 外部系统的字段（多层结构可逗号分割）
     */
    @TableField(value = "api_field")
    private String apiField;

}
