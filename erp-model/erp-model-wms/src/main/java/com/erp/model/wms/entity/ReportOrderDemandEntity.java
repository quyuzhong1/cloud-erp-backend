package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2024-09-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("report_order_demand")
public class ReportOrderDemandEntity extends BaseEntity<ReportOrderDemandEntity> {

    /**
    * 仓库id 
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 虚拟仓库id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 虚拟出库名称
    */
    @TableField("virtual_warehouse_name")
    private String virtualWarehouseName;
    /**
    * 实体仓名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 剩余需求总数
    */
    @TableField("total_qty")
    private Integer totalQty;
    /**
    * B2B销售订单需求数
    */
    @TableField("so_qty")
    private Integer soQty;
    /**
    * B2C销售订单需求数
    */
    @TableField("b2c_so_qty")
    private Integer b2cSoQty;
    /**
    * 头程需求数
    */
    @TableField("first_mile_qty")
    private Integer firstMileQty;
    /**
    * 虚拟仓可用库存
    */
    @TableField("virtual_usable_qty")
    private Integer virtualUsableQty;
    /**
    * 是否缺货，true是，false否
    */
    @TableField("is_virtual_scarce")
    private Boolean isVirtualScarce;
    /**
    * 缺货数量
    */
    @TableField("virtual_scarce_qty")
    private Integer virtualScarceQty;

    /**
     * 实体仓未分配
     */
    @TableField("un_distribution_qty")
    private Integer unDistributionQty;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_NAME = "virtual_warehouse_name";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String TOTAL_QTY = "total_qty";

    public static final String SO_QTY = "so_qty";

    public static final String B2C_SO_QTY = "b2c_so_qty";

    public static final String FIRST_MILE_QTY = "first_mile_qty";

    public static final String VIRTUAL_USABLE_QTY = "virtual_usable_qty";

    public static final String IS_VIRTUAL_SCARCE = "is_virtual_scarce";

    public static final String VIRTUAL_SCARCE_QTY = "virtual_scarce_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}