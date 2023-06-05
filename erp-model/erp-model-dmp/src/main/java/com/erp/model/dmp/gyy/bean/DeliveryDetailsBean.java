package com.erp.model.dmp.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ToString
public class DeliveryDetailsBean {
    /**
     * qty : 1.0
     * discount : 1.0
     * refund : 0
     * itemCategoryName : null
     * itemUnitName : null
     * barcode : 3190
     * tariff : 0.0
     * memo : null
     * picUrl : null
     * oid : null
     * discount_fee : 0
     * amount_after : 0
     * lack : 0
     * trade_code : SO524352464025
     * origin_price : 0.0
     * origin_amount : 0.0
     * platform_item_name : null
     * platform_sku_name : null
     * item_id : 524228892871
     * item_sku_id : null
     * item_code : 3190
     * item_name : 引流卡
     * sku_code : null
     * sku_name : null
     * sku_note : null
     * combine_item_code : null
     * location_code :
     * platform_code : 220913-323186933220499
     * tax_rate : 0.0
     * tax_amount : 0.0
     * order_type : Sales
     * platform_flag : 0
     * detail_unique : null
     * detail_batch : null
     * is_gift : 1
     * businessman_name : 吴晓锋
     * item_add_attribute : 0
     * gift_source_view : 【满就送赠品】-【买就送2922（A5版本）】
     * currency_code : null
     * currency_name : null
     * tax_no : null
     * post_cost : 0
     * other_service_fee : 0
     * total_cost_price : 0
     * price : 0
     * amount : 0
     * post_fee : 0
     * plat_discount_amount : null
     * distribution_post_fee : null
     * sale_unit_name : null
     * exchange_rate : null
     * lack_qty : 0.0
     * lack_reason : null
     */

    /**
     * 子订单号
     */
    @SerializedName("oid")
    private String oid;
    /**
     * 数量
     */
    @SerializedName("qty")
    private BigDecimal qty;
    /**
     * 折扣
     */
    @SerializedName("discount")
    private BigDecimal discount;
    /**
     * 退款状态 0:未退款 1:退款成功 2:退款中
     */
    @SerializedName("refund")
    private Integer refund;
    /**
     * 分类名称
     */
    @SerializedName("itemCategoryName")
    private String itemCategoryName;
    /**
     * 单位名称
     */
    @SerializedName("itemUnitName")
    private String itemUnitName;
    /**
     * 条形码
     */
    @SerializedName("barcode")
    private String barcode;
    /**
     * 关税
     */
    @SerializedName("tariff")
    private BigDecimal tariff;
    /**
     * 备注
     */
    @SerializedName("memo")
    private String memo;
    /**
     * 图片地址
     */
    @SerializedName("picUrl")
    private String picUrl;
    /**
     * 折扣金额
     */
    @SerializedName("discount_fee")
    private String discountFee;
    /**
     * 让利后金额
     */
    @SerializedName("amount_after")
    private String amountAfter;
    /**
     * 缺货数量
     */
    @SerializedName("lack")
    private Integer lack;
    /**
     * 订单号
     */
    @SerializedName("trade_code")
    private String tradeCode;
    /**
     * 原价
     */
    @SerializedName("origin_price")
    private BigDecimal originPrice;
    /**
     * 原金额
     */
    @SerializedName("origin_amount")
    private BigDecimal originAmount;
    /**
     * 平台商品名称
     */
    @SerializedName("platform_item_name")
    private String platformItemName;
    /**
     * 平台sku名称
     */
    @SerializedName("platform_sku_name")
    private String platformSkuName;
    /**
     * 商品id
     */
    @SerializedName("item_id")
    private String itemId;
    /**
     * sku id
     */
    @SerializedName("item_sku_id")
    private String itemSkuId;
    /**
     * 商品编码
     */
    @SerializedName("item_code")
    private String itemCode;
    /**
     * 商品名称
     */
    @SerializedName("item_name")
    private String itemName;
    /**
     * sku编码
     */
    @SerializedName("sku_code")
    private String skuCode;
    /**
     * sku名称
     */
    @SerializedName("sku_name")
    private String skuName;
    /**
     * sku备注
     */
    @SerializedName("sku_note")
    private String skuNote;
    /**
     * 组合商品编码
     */
    @SerializedName("combine_item_code")
    private String combineItemCode;
    /**
     * 仓库编码
     */
    @SerializedName("location_code")
    private String locationCode;
    /**
     * 平台编码
     */
    @SerializedName("platform_code")
    private String platformCode;
    /**
     * 税率
     */
    @SerializedName("tax_rate")
    private BigDecimal taxRate;
    /**
     * 税额
     */
    @SerializedName("tax_amount")
    private BigDecimal taxAmount;
    /**
     * 订单类型
     */
    @SerializedName("order_type")
    private String orderType;
    /**
     * 平台标记
     */
    @SerializedName("platform_flag")
    private Integer platformFlag;
    /**
     * 明细唯一标识
     */
    @SerializedName("detail_unique")
    private String detailUnique;
    /**
     * 明细批次
     */
    @SerializedName("detail_batch")
    private String detailBatch;
    /**
     * 是否赠品
     */
    @SerializedName("is_gift")
    private Boolean isGift;
    /**
     * 商家名称
     */
    @SerializedName("businessman_name")
    private String businessmanName;
    /**
     * 是否加属性
     */
    @SerializedName("item_add_attribute")
    private Integer itemAddAttribute;
    /**
     * 赠品来源
     */
    @SerializedName("gift_source_view")
    private String giftSourceView;
    /**
     * 币别代码
     */
    @SerializedName("currency_code")
    private String currencyCode;
    /**
     * 币别名称
     */
    @SerializedName("currency_name")
    private String currencyName;
    /**
     * 税号
     */
    @SerializedName("tax_no")
    private String taxNo;
    /**
     * 物流成本
     */
    @SerializedName("post_cost")
    private BigDecimal postCost;
    /**
     * 其他服务费
     */
    @SerializedName("other_service_fee")
    private BigDecimal otherServiceFee;
    @SerializedName("total_cost_price")
    private BigDecimal totalCostPrice;
    /**
     * 实际单价
     */
    @SerializedName("price")
    private BigDecimal price;
    /**
     * 实际金额
     */
    @SerializedName("amount")
    private BigDecimal amount;
    /**
     * 物流费用
     */
    @SerializedName("post_fee")
    private BigDecimal postFee;
    /**
     * 平台折扣金额
     */
    @SerializedName("plat_discount_amount")
    private String platDiscountAmount;
    /**
     * 分销商物流费用
     */
    @SerializedName("distribution_post_fee")
    private String distributionPostFee;
    /**
     * 销售单位名称
     */
    @SerializedName("sale_unit_name")
    private String saleUnitName;
    /**
     * 汇率
     */
    @SerializedName("exchange_rate")
    private String exchangeRate;
    /**
     * 缺货数量
     */
    @SerializedName("lack_qty")
    private BigDecimal lackQty;
    /**
     * 缺货原因
     */
    @SerializedName("lack_reason")
    private String lackReason;
}
