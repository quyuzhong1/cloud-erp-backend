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
 * 仓位分配规则表
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_rule_location_suggest_action")
public class CfgRulePackingActionEntity extends BaseEntity<CfgRulePackingActionEntity> {

    @TableField("rule_id")
    private String ruleId;
    /**
     * 发货仓库
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 库区
     */
    @TableField("warehouse_area_id")
    private String warehouseAreaId;

    /**
     * 出库方式
     */
    @TableField("out_stock_mode")
    private String outStockMode;
    /**
     * 排序
     */
    @TableField("index")
    private Integer index;

    /**
     * 规则类型
     * （PICKING_STRATEGY：拣货仓位推荐;WAREHOUSE_LOCATION_REPLENISH:补货仓位推荐;WAREHOUSE_LOCATION_OUT_STOCK:出库仓位推荐）
     */
    @TableField("rule_type")
    private String ruleType;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
