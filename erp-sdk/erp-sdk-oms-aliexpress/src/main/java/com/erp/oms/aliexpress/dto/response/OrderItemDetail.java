package com.erp.oms.aliexpress.dto.response;

import com.google.gson.annotations.SerializedName;
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
    @SerializedName("product_count")
    private Integer productCount;

    /**
     *物流类型，买家选择的物流方式
     */
    @SerializedName("logistics_type")
    private String logisticsType;


    /**
     *商品ID
     */
    @SerializedName("product_id")
    private Long productId;


    /**
     *商品名
     */
    @SerializedName("product_name")
    private String productName;

    /**
     *商品单价
     */
    @SerializedName("product_price")
    private AmountInfo productPrice;


    /**
     *SKU信息
     */
    @SerializedName("sku_code")
    private String skuCode;


    /**
     *商品主图Url
     */
    @SerializedName("product_img_url")
    private String productImgUrl;


    /**
     *买家备注(子订单级别)
     */
    @SerializedName("buyer_memo")
    private String buyerMemo;

    /**
     *
     * 子订单中的各种标
     */
    @SerializedName("tags")
    private List<String> tags;

    /**
     * 子订单ID
     */
    @SerializedName("child_order_id")
    private String childOrderId;


    /**
     * cainiaoInternationalWarehouse
     * 表示是菜鸟认证海外仓发货的，这类订单（子订单）将由菜鸟系统下发海外仓系统，进行订单履行，
     * 商家ERP进行过滤此类型的订单（子订单）。
     * 其他情况为空
     */
    @SerializedName("logistics_warehouse_type")
    private String logisticsWarehouseType;

    /**
     * U_TAXED海外仓已税，跨境已税是I_TAXED
     */
    @SerializedName("already_taxed")
    private String alreadyTaxed;

    /**
     * 物流服务名称 （买家物流）
     */
    @SerializedName("logistics_service_name")
    private String logisticsServiceName;


}
