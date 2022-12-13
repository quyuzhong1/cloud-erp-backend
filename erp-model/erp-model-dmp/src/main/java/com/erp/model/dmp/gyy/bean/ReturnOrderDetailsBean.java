package com.erp.model.dmp.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@NoArgsConstructor
public class ReturnOrderDetailsBean {
    /**
     * id : 315677284382
     * qty : 2.0
     * note : 7天无理由退换货
     * oid : null
     * discount : 1.0
     * total_cost_price : 0
     * cost_price : 0
     * post_fee : null
     * sku_code : null
     * item_sku_id : null
     * sku_note : null
     * item_name : null
     * amount_after : 0
     * item_code : null
     * item_id : null
     * real_in : 0.0
     * price : 146.0000
     * amount : 292.0000
     * discount_fee : 0
     * is_gift : 0
     * detail_batch : null
     * detail_unique : null
     * combine_item_code : null
     * other_service_fee : 0.0
     * location_code : null
     * platform_code : 1560753362709029282
     * origin_amount : 0
     * origin_price : 0
     * location_name : null
     * item_unit_name : null
     */

    @SerializedName("id")
    private Long id;
    @SerializedName("qty")
    private Integer qty;
    @SerializedName("note")
    private String note;
    @SerializedName("oid")
    private String oid;
    @SerializedName("discount")
    private BigDecimal discount;
    @SerializedName("total_cost_price")
    private BigDecimal totalCostPrice;
    @SerializedName("cost_price")
    private String costPrice;
    @SerializedName("post_fee")
    private String postFee;
    @SerializedName("sku_code")
    private String skuCode;
    @SerializedName("item_sku_id")
    private String itemSkuId;
    @SerializedName("sku_note")
    private String skuNote;
    @SerializedName("item_name")
    private String itemName;
    @SerializedName("amount_after")
    private String amountAfter;
    @SerializedName("item_code")
    private String itemCode;
    @SerializedName("item_id")
    private String itemId;
    @SerializedName("real_in")
    private BigDecimal realIn;
    @SerializedName("price")
    private String price;
    @SerializedName("amount")
    private BigDecimal amount;
    @SerializedName("discount_fee")
    private String discountFee;
    @SerializedName("is_gift")
    private Integer isGift;
    @SerializedName("detail_batch")
    private String detailBatch;
    @SerializedName("detail_unique")
    private String detailUnique;
    @SerializedName("combine_item_code")
    private String combineItemCode;
    @SerializedName("other_service_fee")
    private BigDecimal otherServiceFee;
    @SerializedName("location_code")
    private String locationCode;
    @SerializedName("platform_code")
    private String platformCode;
    @SerializedName("origin_amount")
    private String originAmount;
    @SerializedName("origin_price")
    private String originPrice;
    @SerializedName("location_name")
    private String locationName;
    @SerializedName("item_unit_name")
    private String itemUnitName;
}
