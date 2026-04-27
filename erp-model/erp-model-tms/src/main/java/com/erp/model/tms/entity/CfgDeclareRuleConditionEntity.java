package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 报关规则条件表
 * </p>
 *
 * @author jack
 * @since 2026-04-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_declare_rule_condition")
public class CfgDeclareRuleConditionEntity extends BaseEntity<CfgDeclareRuleConditionEntity> {

    /**
    * 规则主表id
    */
    @TableField("rule_id")
    private String ruleId;
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
    /**
    * 比较符
    */
    @TableField("compare")
    private String compare;
    /**
    * 值
    */
    @TableField("value")
    private String value;
    /**
    * 值对应名称
    */
    @TableField("name")
    private String name;
    /**
    * 右括号
    */
    @TableField("right_bracket")
    private String rightBracket;
    /**
    * 逻辑关系: or=或, and=且
    */
    @TableField("logic")
    private String logic;
    /**
    * 顺序
    */
    @TableField("index")
    private Integer index;


    public static final String RULE_ID = "rule_id";

    public static final String LEFT_BRACKET = "left_bracket";

    public static final String FIELD = "field";

    public static final String COMPARE = "compare";

    public static final String VALUE = "value";

    public static final String NAME = "name";

    public static final String RIGHT_BRACKET = "right_bracket";

    public static final String LOGIC = "logic";

    public static final String INDEX = "index";

    @Override
    public Serializable pkVal() {
        return null;
    }

}