package com.erp.model.mrp.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 日志字段配置表
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_operate_log_field")
@EqualsAndHashCode(callSuper = true)
public class CfgOperateLogFieldEntity extends BaseEntity<CfgOperateLogFieldEntity> {

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
     * 字段类型 0字符串，1是或否，2枚举
     */
    @TableField("type")
    private Integer type;

    /**
     * 枚举类型
     */
    @TableField("enum_class")
    private String enumClass;

    /**
     * true|false对应值,竖线分隔
     */
    @TableField("boolean_value")
    private String booleanValue;


    public static final String FIELD = "field";

    public static final String FIELD_NAME = "field_name";

    public static final String CLASS_PATH = "class_path";

    public static final String TYPE = "type";

    public static final String ENUM_CLASS = "enum_class";

    public static final String BOOLEAN_VALUE = "boolean_value";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
