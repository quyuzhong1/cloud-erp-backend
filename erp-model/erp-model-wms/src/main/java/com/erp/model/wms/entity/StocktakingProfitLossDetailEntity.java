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
 * 盘盈盘亏单详情
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("stocktaking_profit_loss_detail")
public class StocktakingProfitLossDetailEntity extends BaseEntity<StocktakingProfitLossDetailEntity> {

    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

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
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 仓库id
     */
    @TableField(exist = false)
    private String warehouseName;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 盘点数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 差异数量
     */
    @TableField("diff_qty")
    private Integer diffQty;

    /**
     * 可用库存
     */
    @TableField("usable_qty")
    private Integer usableQty;

    /**
     * 冻结数量
     */
    @TableField("frozen_qty")
    private Integer frozenQty;

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;


    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String STOCKTAKING_QTY = "stocktaking_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String USABLE_QTY = "usable_qty";

    public static final String FROZEN_QTY = "frozen_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
