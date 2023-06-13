package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * 委外订单明细
 * </p>
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("subcontract_order_detail")
public class SubcontractOrderDetailEntity extends BaseEntity<SubcontractOrderDetailEntity> {


    /**
    * 主表id
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
    * 变体信息
    */
    @TableField("variant_property")
    private String variantProperty;

    /**
    * 到货状态（0未到货，1部分到货，2已到货）
    */
    @TableField("arrival_status")
    private String arrivalStatus;

    /**
    * 到货时间
    */
    @TableField("arrival_time")
    private LocalDateTime arrivalTime;

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;

    /**
    * 采购数量
    */
    @TableField("qty")
    private Integer qty;

    /**
    * 领料数量(发料数量)
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;

    /**
    * 含税单价
    */
    @TableField("price")
    private BigDecimal price;

    /**
    * 币别
    */
    @TableField("currency")
    private String currency;

    /**
    * 币种符号
    */
    @TableField("currency_symbol")
    private String currencySymbol;

    /**
    * 采购金额
    */
    @TableField("amount")
    private BigDecimal amount;

    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
    * 预计交货日期
    */
    @TableField("plan_delivery_date")
    private LocalDate planDeliveryDate;

    /**
    * 是否是赠品（false否，true是）
    */
    @TableField("is_gift")
    private Boolean isGift;

    /**
    * 是否加急（false否，true是）
    */
    @TableField("is_urgent")
    private Boolean isUrgent;

    /**
    * bom版本
    */
    @TableField("bom_version")
    private Integer bomVersion;

    /**
    * 是否自动生成采购订单
    */
    @TableField("is_generate_po")
    private Boolean isGeneratePo;

    /**
    * 父级SKUid
    */
    @TableField("parent_id")
    private String parentId;

    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 是否结束交货
     */
    @TableField("is_end_receive")
    private Boolean isEndReceive;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String VARIANT_PROPERTY = "variant_property";

    public static final String ARRIVAL_STATUS = "arrival_status";

    public static final String ARRIVAL_TIME = "arrival_time";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String QTY = "qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String PRICE = "price";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String AMOUNT = "amount";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String IS_GIFT = "is_gift";

    public static final String IS_URGENT = "is_urgent";

    public static final String BOM_VERSION = "bom_version";

    public static final String IS_GENERATE_PO = "is_generate_po";

    public static final String PARENT_ID = "parent_id";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String IS_END_RECEIVE = "is_end_receive";


    @Override
    public Serializable pkVal() {
        return null;
    }

}