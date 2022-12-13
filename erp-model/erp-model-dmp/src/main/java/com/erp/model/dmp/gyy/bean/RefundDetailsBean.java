package com.erp.model.dmp.gyy.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@ToString
public class RefundDetailsBean {
    /**
     * qty : 1.0
     * discount : null
     * note : null
     * item_id : 313099370112
     * item_sku_id : null
     * item_code : 1781
     * sku_code : null
     * other_service_fee : 0.0
     * item_unit_name : null
     * discount_fee : 0.0
     * origin_amount : 0
     * amount : 1.0000
     * origin_price : null
     * price : 1.0000
     */

    @SerializedName("qty")
    private Integer qty;
    @SerializedName("discount")
    private String discount;
    @SerializedName("note")
    private String note;
    @SerializedName("item_id")
    private String itemId;
    @SerializedName("item_sku_id")
    private String itemSkuId;
    @SerializedName("item_code")
    private String itemCode;
    @SerializedName("sku_code")
    private String skuCode;
    @SerializedName("other_service_fee")
    private BigDecimal otherServiceFee;
    @SerializedName("item_unit_name")
    private String itemUnitName;
    @SerializedName("discount_fee")
    private BigDecimal discountFee;
    @SerializedName("origin_amount")
    private String originAmount;
    @SerializedName("amount")
    private String amount;
    @SerializedName("origin_price")
    private String originPrice;
    @SerializedName("price")
    private String price;
}
