package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 策略（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-10-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_order_strategy")
public class CfgRuleOrderStrategyEntity extends BaseEntity<CfgRuleOrderStrategyEntity> {
    /**
    * 采购建议策略,是否拆分组合品
    */
    @TableField("is_split")
    private Boolean isSplit;

    /**
     * 是否合并SKU集中采购
     */
    @TableField("is_merge_sku")
    private Boolean isMergeSku;

    public static final String IS_SPLIT = "is_split";

    public static final String IS_MERGE_SKU = "is_merge_sku";

    @Override
    public Serializable pkVal() {
        return null;
    }

}