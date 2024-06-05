package com.erp.oms.aliexpress.dto.response;

import cn.hutool.core.annotation.Alias;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AliExpressDeliveryDetail implements Serializable {

    /**
     * 库存数量
     */
    @Alias("aic_inventory")
    private String stockQty;


    /**
     * 货品实际支付金额
     */
    @Alias("sku_actual_paid_amount")
    private String actualPaidAmount;


    /**
     * 货品优惠金额
     */
    @Alias("sku_discount_amount")
    private ReceiptInfo discountAmount;


    /**
     * SKU单价
     */
    @Alias("unit_price")
    private BuyerInfo  unitPrice;

    /**
     * 发货数量
     */
    @Alias("order_line_qty")
    private String deliveryQty;


//    /**
//     * 货品条码
//     */
//    @Alias("barcode")
//    private String barcode;


    /**
     * 货品Id
     */
    @Alias("sc_item_id")
    private String scItemId;


    @Alias("barcode")
    private String platformSku;

    /**
     *  skuID
     */
    @Alias("sku_id")
    private String platformSkuId;

    /**
     * 商品名称
     */
    @Alias("item_title")
    private String  itemTitle;


    /**
     * 商品ID
     */
    @Alias("item_id")
    private String itemId;


    /**
     * 扩展字段
     */
    @Alias("extend_fields")
    private String extendFields;

    /**
     * 发货仓库名称
     */
    private String warehouseName;


}
