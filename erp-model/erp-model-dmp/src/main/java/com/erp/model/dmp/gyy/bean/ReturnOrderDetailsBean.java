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

    /**
     * 行ID
     */
    @SerializedName("id")
    private Long id;
    /**
     * 数量
     */
    @SerializedName("qty")
    private Integer qty;
    /**
     *  备注
     */
    @SerializedName("note")
    private String note;
    /**
     * 子订单号
     */
    @SerializedName("oid")
    private String oid;
    /**
     * 折扣
     */
    @SerializedName("discount")
    private BigDecimal discount;
    /**
     * 退款成本
     */
    @SerializedName("total_cost_price")
    private BigDecimal totalCostPrice;
    /**
     * 成本价
     */
    @SerializedName("cost_price")
    private String costPrice;
    /**
     * 物流费用
     */
    @SerializedName("post_fee")
    private String postFee;
    /**
     * 规格代码
     */
    @SerializedName("sku_code")
    private String skuCode;
    /**
     * 规格ID
     */
    @SerializedName("item_sku_id")
    private String itemSkuId;
    /**
     * 规格备注
     */
    @SerializedName("sku_note")
    private String skuNote;
    /**
     * 商品名称
     */
    @SerializedName("item_name")
    private String itemName;
    /**
     * 让利后金额
     */
    @SerializedName("amount_after")
    private String amountAfter;
    /**
     * 商品代码
     */
    @SerializedName("item_code")
    private String itemCode;
    /**
     * 商品ID
     */
    @SerializedName("item_id")
    private String itemId;
    /**
     * 实际入库数
     */
    @SerializedName("real_in")
    private BigDecimal realIn;
    /**
     * 单价
     */
    @SerializedName("price")
    private String price;
    /**
     * 金额
     */
    @SerializedName("amount")
    private BigDecimal amount;
    /**
     * 折扣金额
     */
    @SerializedName("discount_fee")
    private String discountFee;
    /**
     * 是否赠品 0:不是 1:是
     */
    @SerializedName("is_gift")
    private Integer isGift;
    /**
     * 批次号
     */
    @SerializedName("detail_batch")
    private String detailBatch;
    /**
     * 唯一码列表
     */
    @SerializedName("detail_unique")
    private String detailUnique;
    /**
     * 组合商品代码
     */
    @SerializedName("combine_item_code")
    private String combineItemCode;
    /**
     * 其他服务费
     */
    @SerializedName("other_service_fee")
    private BigDecimal otherServiceFee;
    /**
     * 库位代码
     */
    @SerializedName("location_code")
    private String locationCode;
    /**
     * 平台单号
     */
    @SerializedName("platform_code")
    private String platformCode;
    /**
     * 原始金额
     */
    @SerializedName("origin_amount")
    private String originAmount;
    /**
     * 原始单价
     */
    @SerializedName("origin_price")
    private String originPrice;
    /**
     * 库位名称
     */
    @SerializedName("location_name")
    private String locationName;
    /**
     * 单位名称
     */
    @SerializedName("item_unit_name")
    private String itemUnitName;
}
