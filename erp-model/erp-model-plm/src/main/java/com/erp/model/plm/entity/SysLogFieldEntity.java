package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 18:12
 */
@Data
@TableName("sys_log_field")
@Accessors(chain = true)
public class SysLogFieldEntity {


    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 字段
     */
    @TableField("field")
    private String field;

    /**
     * 字段名称
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 类路径
     */
    @TableField("class_path")
    private String classPath;

    /**
     * 字段类型 0字符串，1是或否，2枚举，3字典,4人员
     * 枚举需要实现EnumMessage，保持字段名称一致
     */
    private Integer type;

    /**
     * 枚举类
     */
    private String enumClass;

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
