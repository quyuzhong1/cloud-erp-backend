package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 仓位安全库存
 * @date 2024-06-21
 * @author tanmujin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("warehouse_location_safety_inventory")
public class WarehouseLocationSafetyInventoryEntity extends BaseEntity<WarehouseLocationSafetyInventoryEntity> {

    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 仓位编码
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 仓库ID
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 库区编码
     */
    @TableField("warehouse_area")
    private String warehouseArea;

    /**
     * 库区类型
     */
    @TableField("warehouse_area_type")
    private String warehouseAreaType;

    /**
     * 安全库存
     */
    @TableField("safety_qty")
    private Integer safetyQty;

    /**
     * 补货上限量
     */
    @TableField("max_qty")
    private Integer maxQty;
}
