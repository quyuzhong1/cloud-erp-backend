package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 规则条件表
 * </p>
 *
 * @author jack
 * @since 2025-05-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_condition")
public class CfgRuleConditionEntity extends BaseEntity<CfgRuleConditionEntity> {

    /**
    * 左括号
    */
    @TableField("left_bracket")
    private String leftBracket;
    /**
    * 条件的字段
    */
    @TableField("field")
    private String field;

    @TableField(exist = false)
    private String fieldName;

    /**
    * 比较符
    */
    @TableField("compare")
    private String compare;
    /**
    * 对应的值
    */
    @TableField("value")
    private String value;
    /**
    * 右括号
    */
    @TableField("right_bracket")
    private String rightBracket;
    /**
    * 逻辑关系 or 和 and
    */
    @TableField("logic")
    private String logic;
    /**
    * 规则id
    */
    @TableField("rule_id")
    private String ruleId;
    /**
    * 顺序
    */
    @TableField("index")
    private Integer index;
    /**
    * 值对应名称
    */
    @TableField("name")
    private String name;
    /**
    * 条件所属规则来源
    */
    @TableField("source_type")
    private String sourceType;

    /**
     * 值类型
     */
    @TableField("value_type")
    private String valueType;


    public static final String LEFT_BRACKET = "left_bracket";

    public static final String FIELD = "field";

    public static final String COMPARE = "compare";

    public static final String VALUE = "value";

    public static final String RIGHT_BRACKET = "right_bracket";

    public static final String LOGIC = "logic";

    public static final String RULE_ID = "rule_id";

    public static final String INDEX = "index";

    public static final String NAME = "name";

    public static final String SOURCE_TYPE = "source_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}