package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 仓位补货数据表实体
 * @date 2024-06-24
 * @author tanmujin
 */
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("warehouse_location_replenish")
public class WarehouseLocationReplenishEntity extends BaseEntity<WarehouseLocationReplenishEntity> implements Serializable {
    /**
     * sku id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 来源单据id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源单号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 实际补货数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 单据状态：LocationReplenishStatusEnum
     */
    @TableField("status")
    private String status;

    /**
     * 取货库区
     */
    @TableField("from_warehouse_area")
    private String fromWarehouseArea;

    /**
     * 取货仓位
     */
    @TableField("from_warehouse_location")
    private String fromWarehouseLocation;

    /**
     * 补货库区
     */
    @TableField("to_warehouse_area")
    private String toWarehouseArea;

    /**
     * 补货仓位
     */
    @TableField("to_warehouse_location")
    private String toWarehouseLocation;

    /**
     * 建议补货数量
     */
    @TableField("suggest_qty")
    private Integer suggestQty;
}
