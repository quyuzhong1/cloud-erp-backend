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
 * 规则关联条件表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("rule_ref_condition")
public class RuleRefConditionEntity extends BaseEntity<RuleRefConditionEntity> {

    /**
    * 规则id
    */
    @TableField("rule_id")
    private String ruleId;
    /**
    * 条件id
    */
    @TableField("rule_condition_id")
    private String ruleConditionId;


    public static final String RULE_ID = "rule_id";

    public static final String RULE_CONDITION_ID = "rule_condition_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}