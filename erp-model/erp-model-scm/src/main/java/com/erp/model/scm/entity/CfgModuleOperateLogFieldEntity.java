package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 日志字段配置表
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_module_operate_log_field")
public class CfgModuleOperateLogFieldEntity extends BaseEntity<CfgModuleOperateLogFieldEntity> {

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


    public static final String FIELD = "field";

    public static final String FIELD_NAME = "field_name";

    public static final String CLASS_PATH = "class_path";

    public static final String TYPE = "type";

    public static final String ENUM_CLASS = "enum_class";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
