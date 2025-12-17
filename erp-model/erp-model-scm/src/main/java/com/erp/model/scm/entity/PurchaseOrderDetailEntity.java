package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_order_detail")
public class PurchaseOrderDetailEntity extends BaseEntity<PurchaseOrderDetailEntity> {



    /**
     * 采购订单id
     */
    @TableField("purchase_order_id")
    private String purchaseOrderId;

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
     * 报关型号
     */
    @TableField("declare_model")
    private String declareModel;

    /**
     * 报关名称
     */
    @TableField("declare_name")
    private String declareName;

    /**
     * 含税单价
     */
    @TableField("tax_price")
    private BigDecimal taxPrice;

    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * 采购数量
     */
    @TableField("purchase_qty")
    private Integer purchaseQty;

    /**
     * 采购金额
     */
    @TableField("purchase_amount")
    private BigDecimal purchaseAmount;

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
     * 新品首批
     */
    @TableField("first_mass_product")
    private String firstMassProduct;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
     * 是否加急（false否，true是）
     */
    @TableField("is_urgent")
    private Boolean isUrgent;

    /**
     * 币种符号
     */
    @TableField("currency_symbol")
    private String currencySymbol;

    /**
     * 变体信息
     */
    @TableField("variant_property")
    private String variantProperty;

    /**
     * 是否结束收货
     */
    @TableField("is_end_receive")
    private Boolean isEndReceive;

    /**
     * 结束交货时间
     */
    @TableField("end_receive_time")
    private LocalDateTime endReceiveTime;

    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;

    /**
     * 库位(委外可用)
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 金蝶详情Id
     */
    @TableField("Kingdee_detail_id")
    private String KingdeeDetailId;

    /**
     * 确认日期
     */
    @TableField("confirm_date")
    private LocalDate confirmDate;

    /**
     * 确认人id
     */
    @TableField("confirm_user_id")
    private String confirmUserId;

    /**
     * 确认人名称
     */
    @TableField("confirm_user_name")
    private String confirmUserName;

    /**
     * 接收说明
     */
    @TableField("confirm_remark")
    private String confirmRemark;

    /**
     * 确认类型 auto系统 手动
     */
    private String confirmType;

    /**
     * 执行状态，ExecutionStatusEnum枚举
     */
    @TableField("execution_status")
    private String executionStatus;



    //----------------------------------------------------------------------辅助字段 -------------------------------------------------------------------


    /**
     * 签收数量
     */
    @TableField(exist = false)
    private Integer receiveQty;

    /**
     * 委外到货状态
     */
    @TableField(exist = false)
    private String subArrivalStatus;

    /**
     * 委外订单类型（child子级，parent父级）
     */
    @TableField(exist = false)
    private String subcontractType;

    /**
     *   采购申请明细id(无需传值，后端使用)
     */
    @TableField(exist = false)
    private String purchaseApplicationDetailId;

    /**
     * 采购申请id(无需传值，后端使用)
     */
    @TableField(exist = false)
    private String purchaseApplicationId;

    /**
     * 采购申请id(无需传值，后端使用)
     */
    @TableField(exist = false)
    private LocalDate purchaseDate;


    public static final String PURCHASE_ORDER_ID = "purchase_order_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String DECLARE_MODEL = "declare_model";

    public static final String DECLARE_NAME = "declare_name";

    public static final String TAX_PRICE = "tax_price";

    public static final String FIELD_CURRENCY = "currency";

    public static final String PURCHASE_QTY = "purchase_qty";

    public static final String PURCHASE_AMOUNT = "purchase_amount";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String IS_GIFT = "is_gift";

    public static final String FIELD_REMARK = "remark";

    public static final String ARRIVAL_STATUS = "arrival_status";

    public static final String ARRIVAL_TIME = "arrival_time";

    public static final String TAX_RATE = "tax_rate";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String STOCK_IN_QTY = "stock_in_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String RETURN_QTY = "return_qty";

    public static final String IS_URGENT = "is_urgent";

    public static final String VARIANT_PROPERTY = "variant_property";


}
