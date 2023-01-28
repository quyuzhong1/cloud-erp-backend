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

    @SerializedName("qty")
    private BigDecimal qty;
    @SerializedName("discount")
    private BigDecimal discount;
    @SerializedName("refund")
    private Integer refund;
    @SerializedName("itemCategoryName")
    private String itemCategoryName;
    @SerializedName("itemUnitName")
    private String itemUnitName;
    @SerializedName("barcode")
    private String barcode;
    @SerializedName("tariff")
    private BigDecimal tariff;
    @SerializedName("memo")
    private String memo;
    @SerializedName("picUrl")
    private String picUrl;
    @SerializedName("oid")
    private String oid;
    @SerializedName("discount_fee")
    private String discountFee;
    @SerializedName("amount_after")
    private String amountAfter;
    @SerializedName("lack")
    private Integer lack;
    @SerializedName("trade_code")
    private String tradeCode;
    @SerializedName("origin_price")
    private BigDecimal originPrice;
    @SerializedName("origin_amount")
    private BigDecimal originAmount;
    @SerializedName("platform_item_name")
    private String platformItemName;
    @SerializedName("platform_sku_name")
    private String platformSkuName;
    @SerializedName("item_id")
    private String itemId;
    @SerializedName("item_sku_id")
    private String itemSkuId;
    @SerializedName("item_code")
    private String itemCode;
    @SerializedName("item_name")
    private String itemName;
    @SerializedName("sku_code")
    private String skuCode;
    @SerializedName("sku_name")
    private String skuName;
    @SerializedName("sku_note")
    private String skuNote;
    @SerializedName("combine_item_code")
    private String combineItemCode;
    @SerializedName("location_code")
    private String locationCode;
    @SerializedName("platform_code")
    private String platformCode;
    @SerializedName("tax_rate")
    private BigDecimal taxRate;
    @SerializedName("tax_amount")
    private BigDecimal taxAmount;
    @SerializedName("order_type")
    private String orderType;
    @SerializedName("platform_flag")
    private Integer platformFlag;
    @SerializedName("detail_unique")
    private String detailUnique;
    @SerializedName("detail_batch")
    private String detailBatch;
    @SerializedName("is_gift")
    private Integer isGift;
    @SerializedName("businessman_name")
    private String businessmanName;
    @SerializedName("item_add_attribute")
    private Integer itemAddAttribute;
    @SerializedName("gift_source_view")
    private String giftSourceView;
    @SerializedName("currency_code")
    private String currencyCode;
    @SerializedName("currency_name")
    private String currencyName;
    @SerializedName("tax_no")
    private String taxNo;
    @SerializedName("post_cost")
    private BigDecimal postCost;
    @SerializedName("other_service_fee")
    private BigDecimal otherServiceFee;
    @SerializedName("total_cost_price")
    private BigDecimal totalCostPrice;
    @SerializedName("price")
    private BigDecimal price;
    @SerializedName("amount")
    private BigDecimal amount;
    @SerializedName("post_fee")
    private BigDecimal postFee;
    @SerializedName("plat_discount_amount")
    private String platDiscountAmount;
    @SerializedName("distribution_post_fee")
    private String distributionPostFee;
    @SerializedName("sale_unit_name")
    private String saleUnitName;
    @SerializedName("exchange_rate")
    private String exchangeRate;
    @SerializedName("lack_qty")
    private BigDecimal lackQty;
    @SerializedName("lack_reason")
    private String lackReason;
}
