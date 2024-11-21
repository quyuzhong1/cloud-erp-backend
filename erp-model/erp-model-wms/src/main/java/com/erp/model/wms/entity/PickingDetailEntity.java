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
 * 拣货明细
 * </p>
 *
 * @author will
 * @since 2023-05-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("picking_detail")
public class PickingDetailEntity extends BaseEntity<PickingDetailEntity> {

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;
    /**
     * platformSkuNo
     */
    @TableField("platform_sku_no")
    private String platformSkuNo;

    /**
     * 数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;


    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;


    @TableField("main_id")
    private String mainId;
    /**
     * 暂存仓位
     */
    @TableField("staging_location")
    private String stagingLocation;
    /**
     * 已拣货数量
     */
    @TableField("picked_qty")
    private Integer pickedQty;
    /**
     * 已分货数量
     */
    @TableField("allocated_qty")
    private Integer allocatedQty;
    /**
     * 是否缺货
     */
    @TableField("is_out_stock")
    private Boolean isOutStock;

    /**
     * 实际拣货数量
     */
    @TableField("actual_qty")
    private Integer actualQty;

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
