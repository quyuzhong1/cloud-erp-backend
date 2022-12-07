package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 17:17
 */
@Data
@TableName("sys_log")
@Accessors(chain = true)
public class SysLogEntity {


    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 类路径
     */
    @TableField("class_path")
    private String classPath;

    /**
     * 字段名称
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 业务id
     */
    @TableField("business_id")
    private String businessId;

    /**
     * 操作
     */
    @TableField("operation")
    private String operation;

    /**
     * 旧值
     */
    @TableField("old_value")
    private String oldValue;


    /**
     * 新值
     */
    @TableField("new_value")
    private String newValue;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private String createUserId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人
     */
    @TableField("create_user_name")
    private String createUserName;

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

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
