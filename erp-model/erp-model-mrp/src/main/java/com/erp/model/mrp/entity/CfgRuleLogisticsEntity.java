package com.erp.model.mrp.entity;

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
 * 备货物流（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_logistics")
public class CfgRuleLogisticsEntity extends BaseEntity<CfgRuleLogisticsEntity> {

    /**
    * 排序字段
    */
    @TableField("index")
    private Integer index;
    /**
    * 物流方式
    */
    @TableField("logistics_method")
    private String logisticsMethod;
    /**
    * 物流时效（天）
    */
    @TableField("logistics_days")
    private Integer logisticsDays;
    /**
    * 发货频率（天）
    */
    @TableField("logistics_cycle_days")
    private Integer logisticsCycleDays;
    /**
    * 备货id（cfg_rule_stock_up）
    */
    @TableField("stock_up_id")
    private String stockUpId;


    public static final String INDEX = "index";

    public static final String LOGISTICS_METHOD = "logistics_method";

    public static final String LOGISTICS_DAYS = "logistics_days";

    public static final String LOGISTICS_CYCLE_DAYS = "logistics_cycle_days";

    public static final String STOCK_UP_ID = "stock_up_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}