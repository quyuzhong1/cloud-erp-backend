package com.erp.model.oms.entity;

import java.math.BigDecimal;
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
 * sku对照表匹配规则
 * </p>
 *
 * @author lrp
 * @since 2023-12-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sku_mapping_rule")
public class SkuMappingRuleEntity extends BaseEntity<SkuMappingRuleEntity> {

    /**
    * 优先级:1-5
    */
    @TableField("priority")
    private BigDecimal priority;
    /**
    * 规则类型:
    */
    @TableField("rule_type")
    private String ruleType;
    /**
    * 规则正则
    */
    @TableField("rule_regular")
    private String ruleRegular;
    /**
    * 扩展规则
    */
    @TableField("extend_rule_type")
    private String extendRuleType;
    /**
    * 扩展规则正则
    */
    @TableField("extend_rule_regular")
    private String extendRuleRegular;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;


    public static final String PRIORITY = "priority";

    public static final String RULE_TYPE = "rule_type";

    public static final String RULE_REGULAR = "rule_regular";

    public static final String EXTEND_RULE_TYPE = "extend_rule_type";

    public static final String EXTEND_RULE_REGULAR = "extend_rule_regular";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}