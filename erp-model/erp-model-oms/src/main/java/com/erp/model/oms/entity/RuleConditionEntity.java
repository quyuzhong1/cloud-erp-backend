package com.erp.model.oms.entity;

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
 * 规则条件表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("rule_condition")
public class RuleConditionEntity extends BaseEntity<RuleConditionEntity> {

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
    * 比较浮
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

    @TableField("rule_id")
    private String ruleId;

    /**
     * 顺序
     */
    @TableField("index")
    private Integer index;

    /**
     * 对应的值类型
     */
    @TableField(exist = false)
    private String valueType;


    public static final String LEFT_BRACKET = "left_bracket";

    public static final String FIELD = "field";

    public static final String DICT_COMPARE = "dict_compare";

    public static final String VALUE = "value";

    public static final String RIGHT_BRACKET = "right_bracket";

    public static final String LOGIC = "logic";

    @Override
    public Serializable pkVal() {
        return null;
    }

}