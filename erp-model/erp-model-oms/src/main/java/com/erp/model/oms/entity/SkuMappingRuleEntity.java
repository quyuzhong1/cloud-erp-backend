package com.erp.model.oms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Map;

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
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "sku_mapping_rule", autoResultMap = true)
@Data
public class SkuMappingRuleEntity extends BaseEntity<SkuMappingRuleEntity> {

    /**
    * 优先级:1-5
    */
    @TableField("priority")
    private Integer priority;
    /**
    * 规则类型
    */
    @TableField("rule_type")
    private String ruleType;
    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;
    /**
    * 规则正则
    */
    @TableField("rule_regex")
    private String ruleRegex;
    /**
    * 扩展规则
    */
    @TableField("extend_rule_type")
    private String extendRuleType;
    /**
    * 扩展规则正则
    */
    @TableField("extend_rule_regex")
    private String extendRuleRegex;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 规则内容
    */
    @TableField(value = "rule_content", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> ruleContent;
    /**
    * 扩展规则内容
    */
    @TableField(value = "extend_rule_content", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extendRuleContent;


    public static final String PRIORITY = "priority";

    public static final String RULE_TYPE = "rule_type";

    public static final String rule_regex = "rule_regex";

    public static final String EXTEND_RULE_TYPE = "extend_rule_type";

    public static final String extend_rule_regex = "extend_rule_regex";

    public static final String DISABLED = "disabled";

    public static final String RULE_CONTENT = "rule_content";

    public static final String EXTEND_RULE_CONTENT = "extend_rule_content";

    @Override
    public Serializable pkVal() {
        return null;
    }

}