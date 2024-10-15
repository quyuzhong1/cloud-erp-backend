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
    * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
    */
    @TableField("platform_type")
    private String platformType;
    /**
    * 采购建议策略,是否拆分
    */
    @TableField("is_split")
    private Boolean isSplit;


    public static final String PLATFORM_TYPE = "platform_type";

    public static final String IS_SPLIT = "is_split";

    @Override
    public Serializable pkVal() {
        return null;
    }

}