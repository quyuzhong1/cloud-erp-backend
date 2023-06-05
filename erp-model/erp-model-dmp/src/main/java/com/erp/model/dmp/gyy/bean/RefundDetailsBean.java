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

    /**
     * 数量
     */
    @SerializedName("qty")
    private Integer qty;
    /**
     * 折扣
     */
    @SerializedName("discount")
    private String discount;
    /**
     * 备注
     */
    @SerializedName("note")
    private String note;
    /**
     * 商品ID
     */
    @SerializedName("item_id")
    private String itemId;
    /**
     * 商品SKU ID
     */
    @SerializedName("item_sku_id")
    private String itemSkuId;
    /**
     * 商品代码
     */
    @SerializedName("item_code")
    private String itemCode;
    /**
     * 规格代码
     */
    @SerializedName("sku_code")
    private String skuCode;
    /**
     * 其他服务费
     */
    @SerializedName("other_service_fee")
    private BigDecimal otherServiceFee;
    /**
     * 商品单位
     */
    @SerializedName("item_unit_name")
    private String itemUnitName;
    /**
     * 让利金额
     */
    @SerializedName("discount_fee")
    private BigDecimal discountFee;
    /**
     * 标准退款金额
     */
    @SerializedName("origin_amount")
    private String originAmount;
    /**
     * 实际退款金额
     */
    @SerializedName("amount")
    private String amount;
    /**
     * 标准单价
     */
    @SerializedName("origin_price")
    private String originPrice;
    /**
     * 单价
     */
    @SerializedName("price")
    private String price;
}
