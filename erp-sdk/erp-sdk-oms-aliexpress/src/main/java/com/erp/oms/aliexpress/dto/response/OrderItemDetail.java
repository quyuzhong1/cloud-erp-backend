package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 产品明细
 * @Author yl
 * @Date 2023-11-29 10:27
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class OrderItemDetail implements Serializable {

    /**
     *商品数量
     */
    @JSONField(name = "product_count")
    private Integer productCount;

    /**
     *商品ID
     */
    @JSONField(name = "product_id")
    private Long productId;


    /**
     *商品名
     */
    @JSONField(name = "product_name")
    private String productName;

    /**
     *商品单价
     */
    @JSONField(name = "product_price")
    private AmountInfo productPrice;


    /**
     *SKU信息
     */
    @JSONField(name = "sku_code")
    private String skuCode;


    /**
     *商品主图Url
     */
    @JSONField(name = "product_img_url")
    private String ProductImgUrl;


    /**
     *买家备注(子订单级别)
     */
    @JSONField(name = "buyer_memo")
    private String buyerMemo;

    /**
     *
     * 子订单中的各种标
     */
    @JSONField(name = "tags")
    private List<String> tags;

    /**
     * 子订单ID
     */
    @JSONField(name = "child_order_id")
    private String childOrderId;


}
