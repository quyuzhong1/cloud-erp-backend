package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * @description: API字段值映射表
 * @author Will
 * @date: 2023/1/11 11:27
 */
@Data
@TableName(value ="cfg_api_field_value_map")
public class CfgApiFieldMapValueEntity implements Serializable {

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
     * 字段映射表id
     */
    @TableField(value = "field_map_id")
    private String fieldMapId;

    /**
     * 本系统的值
     */
    @TableField(value = "self_value")
    private String selfValue;

    /**
     * 外部系统的值
     */
    @TableField(value = "api_value")
    private String apiValue;
}
