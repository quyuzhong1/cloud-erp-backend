package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * </p>
 * 仓位售后推荐
 * </p>
 *
 * @author liuchao
 * @since 2026-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("warehouse_location_suggest_aftersales")
public class WarehouseLocationSuggestAfterSalesEntity extends BaseEntity<WarehouseLocationSuggestAfterSalesEntity> {

    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;
    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
     * 库区编码
     */
    @TableField("warehouse_area_code")
    private String warehouseAreaCode;

    /**
     * 推荐仓位code
     */
    @TableField("suggest_warehouse_location_code")
    private String suggestWarehouseLocationCode;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;
    /**
     * 状态
     * 是否禁用
     * true 禁用
     */
    @TableField("disabled")
    private Boolean disabled;

}
