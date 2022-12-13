package com.erp.model.dmp.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ToString
public class DetailsBean {
    /**
     * oid : 1733215692079667898
     * qty : 1.0
     * price : 99.0
     * amount : 99.0
     * refund : 2
     * discount : 0.5
     * note : 狂欢价 2022年天猫双11狂欢日跨店满减
     * cancel : false
     * del : false
     * weight : 150.0
     * item_code : 3021
     * item_name : ULANZI LT010 Magsafe翻折补光灯 - M-light
     * item_simple_name : ULANZI LT010 Magsafe翻折补光灯
     * sku_name : null
     * sku_code : null
     * post_fee : 0.0
     * discount_fee : 13.33
     * amount_after : 85.67
     * origin_price : 198.0
     * origin_amount : 198.0
     * platform_item_name : 3045||【狂欢价】网红推荐Ulanzi优篮子手机Magsafe磁吸补光灯适用iphone14苹果12/13系列直播拍照打光摄[680722100260]
     * platform_sku_name : 3021||黑色[5051148554699]
     * delivering_qty : 1.0
     * delivered_qty : 1.0
     * tax_rate : 0.0
     * tax_amount : 0.0
     * sku_note : null
     * other_service_fee : 0.0
     * is_gift : false
     * tariff_amount : 0.0
     * item_unit_name : null
     * tax_no : null
     * exchange_rate : 0.0
     * cost_price : 0.0
     * cost_price_total : null
     * plat_discount_amount : 0.0
     * distribution_post_fee : 0.0
     * gift_source_view : null
     * saleable_qty : 0.0
     * pickable_qty : 0.0
     * warehouse_name : null
     * stock_status_name : null
     * estimate_arrived_date : null
     * plan_delivery_date : null
     * maintain_num : null
     * assign_state : null
     * delivery_state : null
     * pre_sale : null
     * warehouse_code : null
     * store_code : null
     * bms_name : null
     * minus_stock : null
     * bms_status : 0
     */

    @SerializedName("oid")
    private String oid;
    @SerializedName("qty")
    private Integer qty;
    @SerializedName("price")
    private BigDecimal price;
    @SerializedName("amount")
    private BigDecimal amount;
    @SerializedName("refund")
    private Integer refund;
    @SerializedName("discount")
    private BigDecimal discount;
    @SerializedName("note")
    private String note;
    @SerializedName("cancel")
    private Boolean cancel;
    @SerializedName("del")
    private Boolean del;
    @SerializedName("weight")
    private BigDecimal weight;
    @SerializedName("item_code")
    private String itemCode;
    @SerializedName("item_name")
    private String itemName;
    @SerializedName("item_simple_name")
    private String itemSimpleName;
    @SerializedName("sku_name")
    private String skuName;
    @SerializedName("sku_code")
    private String skuCode;
    @SerializedName("post_fee")
    private BigDecimal postFee;
    @SerializedName("discount_fee")
    private BigDecimal discountFee;
    @SerializedName("amount_after")
    private BigDecimal amountAfter;
    @SerializedName("origin_price")
    private BigDecimal originPrice;
    @SerializedName("origin_amount")
    private BigDecimal originAmount;
    @SerializedName("platform_item_name")
    private String platformItemName;
    @SerializedName("platform_sku_name")
    private String platformSkuName;
    @SerializedName("delivering_qty")
    private Integer deliveringQty;
    @SerializedName("delivered_qty")
    private BigDecimal deliveredQty;
    @SerializedName("tax_rate")
    private BigDecimal taxRate;
    @SerializedName("tax_amount")
    private BigDecimal taxAmount;
    @SerializedName("sku_note")
    private String skuNote;
    @SerializedName("other_service_fee")
    private BigDecimal otherServiceFee;
    @SerializedName("is_gift")
    private Boolean isGift;
    @SerializedName("tariff_amount")
    private BigDecimal tariffAmount;
    @SerializedName("item_unit_name")
    private String itemUnitName;
    @SerializedName("tax_no")
    private String taxNo;
    @SerializedName("exchange_rate")
    private BigDecimal exchangeRate;
    @SerializedName("cost_price")
    private BigDecimal costPrice;
    @SerializedName("cost_price_total")
    private Object costPriceTotal;
    @SerializedName("plat_discount_amount")
    private BigDecimal platDiscountAmount;
    @SerializedName("distribution_post_fee")
    private BigDecimal distributionPostFee;
    @SerializedName("gift_source_view")
    private String giftSourceView;
    @SerializedName("saleable_qty")
    private BigDecimal saleableQty;
    @SerializedName("pickable_qty")
    private BigDecimal pickableQty;
    @SerializedName("warehouse_name")
    private String warehouseName;
    @SerializedName("stock_status_name")
    private String stockStatusName;
    @SerializedName("estimate_arrived_date")
    private String estimateArrivedDate;
    @SerializedName("plan_delivery_date")
    private String planDeliveryDate;
    @SerializedName("maain_num")
    private String maainNum;
    @SerializedName("assign_state")
    private Integer assignState;
    @SerializedName("delivery_state")
    private Integer deliveryState;
    @SerializedName("pre_sale")
    private String preSale;
    @SerializedName("warehouse_code")
    private String warehouseCode;
    @SerializedName("store_code")
    private String storeCode;
    @SerializedName("bms_name")
    private String bmsName;
    @SerializedName("minus_stock")
    private String minusStock;
    @SerializedName("bms_status")
    private Integer bmsStatus;

    @Override
    public String toString() {
        return "DetailsBean{" +
                "oid='" + oid + '\'' +
                ", qty=" + qty +
                ", price=" + price +
                ", refund=" + refund +
                ", note='" + note + '\'' +
                ", skuName='" + skuName + '\'' +
                ", skuCode='" + skuCode + '\'' +
                ", skuNote='" + skuNote + '\'' +
                ", isGift=" + isGift +
                ", itemUnitName='" + itemUnitName + '\'' +
                ", costPriceTotal=" + costPriceTotal +
                ", platDiscountAmount=" + platDiscountAmount +
                ", distributionPostFee=" + distributionPostFee +
                ", saleableQty=" + saleableQty +
                ", pickableQty=" + pickableQty +
                ", stockStatusName='" + stockStatusName + '\'' +
                ", estimateArrivedDate='" + estimateArrivedDate + '\'' +
                ", planDeliveryDate='" + planDeliveryDate + '\'' +
                ", assignState='" + assignState + '\'' +
                ", deliveryState='" + deliveryState + '\'' +
                '}';
    }
}
