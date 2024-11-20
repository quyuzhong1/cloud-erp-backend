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
 * 订单销量表
 * </p>
 *
 * @author will
 * @since 2024-09-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("report_order_sales")
public class ReportOrderSalesEntity extends BaseEntity<ReportOrderSalesEntity> {

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
    * 今日销量
    */
    @TableField("today_sales_qty")
    private Integer todaySalesQty;
    /**
    * 昨日销量
    */
    @TableField("yesterday_sales_qty")
    private Integer yesterdaySalesQty;
    /**
    * 近3天销量
    */
    @TableField("three_days_sales_qty")
    private Integer threeDaysSalesQty;
    /**
    * 近7天销量
    */
    @TableField("seven_days_sales_qty")
    private Integer sevenDaysSalesQty;
    /**
    * 近14天销量
    */
    @TableField("fourteen_days_sales_qty")
    private Integer fourteenDaysSalesQty;
    /**
    * 30天销量
    */
    @TableField("thirty_days_sales_qty")
    private Integer thirtyDaysSalesQty;
    /**
     * 60天销量
     */
    @TableField("sixty_days_sales_qty")
    private Integer sixtyDaysSalesQty;
    /**
     * 90天销量
     */
    @TableField("ninety_days_sales_qty")
    private Integer ninetyDaysSalesQty;
    /**
    * 是否缺货，true是，false否
    */
    @TableField("is_virtual_scarce")
    private Boolean isVirtualScarce;
    /**
    * 是否预警，true是，false否
    */
    @TableField("is_warn")
    private Boolean isWarn;
    /**
    * 缺货数量
    */
    @TableField("virtual_scarce_qty")
    private Integer virtualScarceQty;
    /**
    * 剩余需求总数
    */
    @TableField("total_qty")
    private Integer totalQty;
    /**
     * b2b需求总数
     */
    @TableField("b2b_qty")
    private Integer b2bQty;
    /**
     * b2c需求总数
     */
    @TableField("b2c_qty")
    private Integer b2cQty;
    /**
     * 头程需求总数
     */
    @TableField("first_mile_qty")
    private Integer firstMileQty;
    /**
    * 虚拟仓可用库存
    */
    @TableField("virtual_usable_qty")
    private Integer virtualUsableQty;
    /**
    * 虚拟仓冻结库存
    */
    @TableField("virtual_frozen_qty")
    private Integer virtualFrozenQty;
    /**
    * 虚拟仓库存
    */
    @TableField("virtual_total_qty")
    private Integer virtualTotalQty;
    /**
    * 已出库数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 已分配数量
    */
    @TableField("distribution_qty")
    private Integer distributionQty;

    /**
     * 近30天虚拟仓库存
     */
    @TableField("thirty_days_virtual_qty")
    private Integer thirtyDaysVirtualQty;

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String VIRTUAL_WAREHOUSE_NAME = "virtual_warehouse_name";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String TODAY_SALES_QTY = "today_sales_qty";

    public static final String YESTERDAY_SALES_QTY = "yesterday_sales_qty";

    public static final String THREE_DAYS_SALES_QTY = "three_days_sales_qty";

    public static final String SEVEN_DAYS_SALES_QTY = "seven_days_sales_qty";

    public static final String FOURTEEN_DAYS_SALES_QTY = "fourteen_days_sales_qty";

    public static final String THIRTY_DAYS_SALES_QTY = "thirty_days_sales_qty";

    public static final String IS_VIRTUAL_SCARCE = "is_virtual_scarce";

    public static final String IS_WARN = "is_warn";

    public static final String VIRTUAL_SCARCE_QTY = "virtual_scarce_qty";

    public static final String TOTAL_QTY = "total_qty";

    public static final String VIRTUAL_USABLE_QTY = "virtual_usable_qty";

    public static final String VIRTUAL_FROZEN_QTY = "virtual_frozen_qty";

    public static final String VIRTUAL_TOTAL_QTY = "virtual_total_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String DISTRIBUTION_QTY = "distribution_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}