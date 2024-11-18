package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * SYS系统日志字段保存配置表
 * </p>
 *
 * @author Jim
 * @since 2023-08-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("sys_log_record_field")
public class SysLogRecordFieldEntity extends BaseEntity<SysLogRecordFieldEntity> {

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
     * 字段类型 0字符串，1是或否，2枚举，3字典，4人员
     */
    @TableField("type")
    private Integer type;

    /**
     * 枚举类型(用于枚举值转换,需要枚举整个路径)
     */
    @TableField("enum_class")
    private String enumClass;

    /**
     * true|false对应值,竖线分隔
     */
    @TableField("boolean_value")
    private String booleanValue;


    public static final String FIELD_FIELD = "field";

    public static final String FIELD_NAME = "field_name";

    public static final String CLASS_PATH = "class_path";

}