package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 日志字段配置表
 * </p>
 *
 * @author will
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_operate_log_field")
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
    * 字段类型 0字符串，1是或否，2枚举， 3字典
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
    /**
    * 当类型3时存储字典类型
    */
    @TableField("value")
    private String value;


    public static final String FIELD = "field";

    public static final String FIELD_NAME = "field_name";

    public static final String CLASS_PATH = "class_path";

    public static final String TYPE = "type";

    public static final String ENUM_CLASS = "enum_class";

    public static final String BOOLEAN_VALUE = "boolean_value";

    public static final String VALUE = "value";

    @Override
    public Serializable pkVal() {
        return null;
    }

}