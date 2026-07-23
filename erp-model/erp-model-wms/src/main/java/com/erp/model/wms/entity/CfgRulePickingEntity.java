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
 * 仓位推荐表
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_rule_location_suggest")
public class CfgRulePickingEntity extends BaseEntity<CfgRulePickingEntity> {

    /**
     * 规则名称
     */
    @TableField("name")
    private String name;

    /**
     * 禁用状态 false 未禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    /**
     * 拣货禁用状态 false 未禁用
     */
    @TableField("pick_disabled")
    private Boolean pickDisabled;
    /**
     * 补货禁用状态 false 未禁用
     */
    @TableField("replenish_disabled")
    private Boolean replenishDisabled;
    /**
     * 出库禁用状态 false 未禁用
     */
    @TableField("out_stock_disabled")
    private Boolean outStockDisabled;
    /**
     * 上架仓位
     *  InWarehouseLocationEnum
     */
    @TableField("in_warehouse_location")
    private String inWarehouseLocation;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
