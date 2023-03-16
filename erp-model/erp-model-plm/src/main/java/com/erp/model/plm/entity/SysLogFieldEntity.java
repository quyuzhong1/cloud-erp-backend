package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
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
public class SysLogFieldEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

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
}
