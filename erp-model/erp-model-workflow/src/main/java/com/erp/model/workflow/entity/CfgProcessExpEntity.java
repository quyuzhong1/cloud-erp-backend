package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 流程设置审核条件
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_process_exp")
public class CfgProcessExpEntity extends BaseEntity<CfgProcessExpEntity> {

    /**
    * 流程设置ID
    */
    @TableField("rule_id")
    private String ruleId;
    /**
    * 条件包含-左括号
    */
    @TableField("left_bracket")
    private String leftBracket;
    /**
    * 选择条件字段
    */
    @TableField("field")
    private String field;
    /**
     * 选择条件字段
     */
    @TableField("field_name")
    private String fieldName;
    /**
    * 条件符号
    */
    @TableField("compare")
    private String compare;
    /**
    * 条件值
    */
    @TableField("value")
    private String value;
    /**
    * 条件包含-右括号
    */
    @TableField("right_bracket")
    private String rightBracket;
    /**
    * 多条件逻辑关系
    */
    @TableField("logic")
    private String logic;
    /**
    * 序号
    */
    @TableField("index")
    private Integer index;
    /**
    * 值对应名称
    */
    @TableField("name")
    private String name;

    /**
     * 值对应名称
     */
    @TableField("value_type")
    private String valueType;


    public static final String RULE_ID = "rule_id";

    public static final String LEFT_BRACKET = "left_bracket";

    public static final String FIELD = "field";

    public static final String FIELD_NAME = "field_name";

    public static final String COMPARE = "compare";

    public static final String VALUE = "value";

    public static final String RIGHT_BRACKET = "right_bracket";

    public static final String LOGIC = "logic";

    public static final String INDEX = "index";

    public static final String NAME = "name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}