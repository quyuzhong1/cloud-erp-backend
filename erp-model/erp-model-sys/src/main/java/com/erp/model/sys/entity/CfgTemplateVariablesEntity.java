package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 模板字段表
 * </p>
 *
 * @author jack
 * @since 2025-07-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_template_variables")
public class CfgTemplateVariablesEntity extends BaseEntity<CfgTemplateVariablesEntity> {

    /**
    * 模板类型
    */
    @TableField("template_type")
    private String templateType;
    /**
    * 分类
    */
    @TableField("type")
    private String type;

    /**
    * 数据库表名
    */
    @TableField("table_name")
    private String tableName;
    /**
    * 字段名称
    */
    @TableField("name")
    private String name;
    /**
    * 字段
    */
    @TableField("field")
    private String field;
    /**
    * 字段类型
    */
    @TableField("field_type")
    private String fieldType;
    /**
    * 默认值
    */
    @TableField("default_value")
    private String defaultValue;
    /**
    * 禁用状态(false:启用,true:禁用)
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 序号
    */
    @TableField("index")
    private Integer index;


    public static final String TEMPLATE_TYPE = "template_type";

    public static final String TYPE = "type";

    public static final String TYPE_NAME = "type_name";

    public static final String TABLE_NAME = "table_name";

    public static final String NAME = "name";

    public static final String FIELD = "field";

    public static final String FIELD_TYPE = "field_type";

    public static final String DEFAULT_VALUE = "default_value";

    public static final String DISABLED = "disabled";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}