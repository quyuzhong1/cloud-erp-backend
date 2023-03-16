package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
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
     * 父级id（用于汇总展示日志）
     */
    @TableField("pid")
    private String pid;

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
     * 主键
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
    private String CreateUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time" , fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 描述
     */
    @TableField(exist = false)
    private String description;
}
