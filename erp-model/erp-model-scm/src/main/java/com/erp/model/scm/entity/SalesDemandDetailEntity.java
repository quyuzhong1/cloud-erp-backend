package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 销售需求明细表
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sales_demand_detail")
public class SalesDemandDetailEntity extends BaseEntity<SalesDemandDetailEntity> {

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
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 单箱数量
     */
    @TableField("unit_qty")
    private Integer unitQty;

    /**
     * 是否加急（false否，true是）
     */
    @TableField("is_urgent")
    private Boolean isUrgent;

    /**
     * 计划交期
     */
    @TableField("plan_delivery_date")
    private Date planDeliveryDate;

    /**
     * 计划备货数量
     */
    @TableField("plan_stock_qty")
    private Integer planStockQty;

    /**
     * 目的仓库id
     */
    @TableField("dest_warehouse_id")
    private String destWarehouseId;

    /**
     * 目的仓库名称
     */
    @TableField("dest_warehouse_name")
    private String destWarehouseName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String UNIT_QTY = "unit_qty";

    public static final String IS_URGENT = "is_urgent";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String PLAN_STOCK_QTY = "plan_stock_qty";

    public static final String DEST_WAREHOUSE_ID = "dest_warehouse_id";

    public static final String DEST_WAREHOUSE_NAME = "dest_warehouse_name";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
