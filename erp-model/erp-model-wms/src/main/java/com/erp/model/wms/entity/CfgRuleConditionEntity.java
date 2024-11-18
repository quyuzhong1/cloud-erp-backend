package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 规则条件表
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Getter
@Setter
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
     * 所属类型
     */
    @TableField("source_type")
    private String sourceType;
    @Override
    public Serializable pkVal() {
        return null;
    }

}
