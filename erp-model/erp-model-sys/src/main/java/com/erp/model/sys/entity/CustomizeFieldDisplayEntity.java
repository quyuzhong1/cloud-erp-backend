package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * (CustomizeFieldDisplay)实体类
 *
 * @author yl
 * @since 2023-02-03 18:52:29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("customize_field_display")
public class CustomizeFieldDisplayEntity implements Serializable {
    private static final long serialVersionUID = -23577872711182914L;
    /**
     * 表id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 创建用户id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更改用户id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 字段标题
     */
    private String fieldTitle;
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 排序
     */
    private Integer orderIndex;
    /**
     * 页面模块编号
     */
    private String moduleCode  ;
    /**
     * 页面模块名称
     */
    private String moduleName  ;



}

