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
 * <p>
 * 售后装箱明细表
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("after_sale_pack_detail")
public class AfterSalePackDetailEntity extends BaseEntity<AfterSalePackDetailEntity> {

    /**
     * 售后装箱id,after_sale_pack.id
     */
    @TableField("main_id")
    private String mainId;
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
     * 仓位id,warehouse_location.id
     */
    @TableField("warehouse_location_id")
    private String warehouseLocationId;
    /**
     * 装箱数量
     */
    @TableField("pack_qty")
    private Integer packQty;
    /**
     * 实际数量
     */
    @TableField("actual_qty")
    private Integer actualQty;
    /**
     * 差异数量
     */
    @TableField("diff_qty")
    private Integer diffQty;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String WAREHOUSE_LOCATION_ID = "warehouse_location_id";

    public static final String PACK_QTY = "pack_qty";

    public static final String ACTUAL_QTY = "actual_qty";

    public static final String DIFF_QTY = "diff_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}