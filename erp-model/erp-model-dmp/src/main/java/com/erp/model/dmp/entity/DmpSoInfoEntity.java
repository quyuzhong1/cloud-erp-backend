package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 中台销售订单表
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_so_info")
public class DmpSoInfoEntity extends BaseEntity<DmpSoInfoEntity> {

    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
    * 平台修改时间
    */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
    /**
    * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
    */
    @TableField("source_platform")
    private String sourcePlatform = "";
    /**
    * 来源平台：gyy，kingdee，mabang
    */
    @TableField("source_system")
    private String sourceSystem = "";
    /**
    * 第三方单据编号
    */
    @TableField("third_code")
    private String thirdCode;
    /**
    * 销售平台原始单号
    */
    @TableField("platform_code")
    private String platformCode = "";
    /**
    * 作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus = Boolean.FALSE;
    /**
     * 是否取消（false未取消，true已取消）
     */
    @TableField("is_cancel")
    private Boolean isCancel = Boolean.FALSE;
    /**
    * 订单状态 waitSubmit.待提交 approveIng.审核中 reject.审核不通过 approve.已审核
    */
    @TableField("order_status")
    private String orderStatus = "";
    /**
    * 发货状态 waitDistribution:待配货，inDistribution:配货中,waitShipped:待发货，shipped：已发货，partialShipped：部分发货
    */
    @TableField("delivery_status")
    private String deliveryStatus = "";
    /**
    * 退货状态 orderReturn：已退货，partialReturn部分退，notReturn：未退货
    */
    @TableField("return_status")
    private String returnStatus = "";
    /**
    * 平台原始状态
    */
    @TableField("platform_original_status")
    private String platformOriginalStatus = "";
    /**
    * 店铺编号
    */
    @TableField("shop_id")
    private String shopId = "";
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName = "";
    /**
    * 卖家备注
    */
    @TableField("sell_remark")
    private String sellRemark = "";
    /**
    * 买家备注
    */
    @TableField("buyer_remark")
    private String buyerRemark = "";
    /**
    * 支付时间
    */
    @TableField("pay_time")
    private LocalDateTime payTime = null;
    /**
    * 付款状态 （false未付款，true已付款）
    */
    @TableField("pay_status")
    private Boolean payStatus;
    /**
    * 付款方式
    */
    @TableField("pay_method")
    private String payMethod = "";
    /**
    * 币种编码
    */
    @TableField("currency_code")
    private String currencyCode = "";
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 订单总金额
    */
    @TableField("pay_amount")
    private BigDecimal payAmount;
    /**
    * 商品总售价
    */
    @TableField("all_amount")
    private BigDecimal allAmount = BigDecimal.ZERO;
    /**
    * 运费收入
    */
    @TableField("shipping_amount")
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    /**
    * 平台费
    */
    @TableField("platform_cost")
    private BigDecimal platformCost;
    /**
    * 补贴金额
    */
    @TableField("subsidy_amount")
    private BigDecimal subsidyAmount;
    /**
    * 发货时间
    */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    /**
    * 物流单号
    */
    @TableField("logistics_code")
    private String logisticsCode = "";
    /**
    * 物流名称
    */
    @TableField("logistics_name")
    private String logisticsName = "";
    /**
     * 审核状态
     */
    @TableField("approve_status")
    private String approveStatus = "";
    /**
    * 拓展字段
    */
    @TableField("extend_data")
    private String extendData = "{}";
    /**
    * 唯一字段md5值
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 数据字段md5值
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 转换id
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 下一层级id
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 物流类型
    */
    @TableField("logistic_type")
    private String logisticType = "";
    /**
    * 渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 渠道名称
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
     * 总优惠金额
     */
    @TableField("total_discount")
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    /**
     * 取消商品总价
     */
    @TableField("total_cancel_goods_amount")
    private BigDecimal totalCancelGoodsAmount = BigDecimal.ZERO;
    /**
     * 取消商品币别
     */
    @TableField("cancel_goods_currency")
    private String cancelGoodsCurrency = "";

    /**
     * 预估货品成本
     */
     @TableField("goods_cost")
     private BigDecimal goodsCost;

    /**
     * VAT税费
     */
    @TableField("vat_cost")
    private BigDecimal vatCost;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 实际运费
     */
    @TableField("actual_shipping_fee")
    private BigDecimal actualShippingFee = BigDecimal.ZERO;
     
    /**
     * 预估运费
     */
    @TableField("estimated_shipping_fee")
    private BigDecimal estimatedShippingFee = BigDecimal.ZERO;

    /**
     * 总税费
     */
    @TableField("total_tax_fee")
    private BigDecimal totalTaxFee = BigDecimal.ZERO;

    /**
     * 总税后支付金额
     */
    @TableField("after_tax_amount")
    private BigDecimal afterTaxAmount = BigDecimal.ZERO;

    /**
     * NF-E发票状态,nfeInvoiceStatus字典
     */
    @TableField("nfe_invoice_status")
    private String nfeInvoiceStatus;
    /**
     * (速卖通)买家视角订单金额
     */
    @TableField("actual_amount")
    private BigDecimal actualAmount = BigDecimal.ZERO;

    /**
     * (速卖通)买家视角订单金额币种
     */
    @TableField("actual_currency")
    private String actualCurrency = "";

    /**
     * (旺店通)订单类型
     * 1、网店销售
     * 2、线下订单
     * 3、售后换货
     * 4、批发业务
     * 7、现款销售
     * 8、分销订单
     */
    @TableField("order_type")
    private String orderType;
    
    /**
     * (旺店通)仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 买家自选物流
     */
    @TableField("buyer_selected_logistics")
    private String buyerSelectedLogistics;


    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String SOURCE_PLATFORM = "source_platform";

    public static final String SOURCE_SYSTEM = "source_system";

    public static final String THIRD_CODE = "third_code";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String ORDER_STATUS = "order_status";

    public static final String DELIVERY_STATUS = "delivery_status";

    public static final String RETURN_STATUS = "return_status";

    public static final String PLATFORM_ORIGINAL_STATUS = "platform_original_status";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String SELL_REMARK = "sell_remark";

    public static final String BUYER_REMARK = "buyer_remark";

    public static final String PAY_TIME = "pay_time";

    public static final String PAY_STATUS = "pay_status";

    public static final String PAY_METHOD = "pay_method";

    public static final String CURRENCY_CODE = "currency_code";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String TAX_RATE = "tax_rate";

    public static final String PAY_AMOUNT = "pay_amount";

    public static final String ALL_AMOUNT = "all_amount";

    public static final String SHIPPING_AMOUNT = "shipping_amount";

    public static final String PLATFORM_COST = "platform_cost";

    public static final String SUBSIDY_AMOUNT = "subsidy_amount";

    public static final String DELIVERY_TIME = "delivery_time";

    public static final String LOGISTICS_CODE = "logistics_code";

    public static final String LOGISTICS_NAME = "logistics_name";

    public static final String EXTEND_DATA = "extend_data";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String LOGISTIC_TYPE = "logistic_type";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    @Override
    public Serializable pkVal() {
        return null;
    }



}