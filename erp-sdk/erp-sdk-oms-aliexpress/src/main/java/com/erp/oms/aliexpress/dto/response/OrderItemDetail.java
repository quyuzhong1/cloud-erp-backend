package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

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
     * 产品数量
     */
    @JSONField(name = "product_count")
    private Integer  productCount;

    /**
     * 时间限制 单位天
     */
    @JSONField(name = "freight_commit_day")
    private String freightCommitDay;

    /**
     * 子订单是否可以提交争议
     */
    @JSONField(name = "can_submit_issue")
    private Boolean canSubmitIssue;

    /**
     * 是否假一赔三
     */
    @JSONField(name = "money_back3x")
    private Boolean moneyBack3x;

    /**
     * 产品url
     */
    @JSONField(name = "product_snap_url")
    private String productSnapUrl;

    /**
     * 买家登录id
     */
    @JSONField(name = "seller_login_id")
    private String sellerLoginId;


    /**
     * 产品id
     */
    @JSONField(name = "product_id")
    private String productId;


    /**
     * 物流服务名称（密钥）
     */
    @JSONField(name = "logistics_type")
    private String logisticsType;

    /**
     * 订单显示状态
     */
    @JSONField(name = "show_status")
    private String showStatus;

    /**
     * 产品图片
     */
    @JSONField(name = "product_img_url")
    private String productImgUrl;

    /**
     * 物流服务名
     */
    @JSONField(name = "logistics_service_name")
    private String LogisticsServiceName;

    /**
     * 货物准备日 单位天
     */
    @JSONField(name = "goods_prepare_time")
    private Integer goodsPrepareTime;

    /**
     * 问题状态
     */
    @JSONField(name = "issue_status")
    private String issue_status;

    /**
     * 产品名称
     */
    @JSONField(name = "product_name")
    private String productName;


    /**
     *产品单位
     */
    @JSONField(name = "product_unit")
    private String productUnit;

    /**
     *发货人类型。“SELLER_SEND_GOODS”：卖家发货；“WAREHOUSE_SEND_GOODS”：仓库交货
     */
    @JSONField(name = "send_goods_operator")
    private String sendGoodsOperator;

    /**
     * 子单id
     */
    @JSONField(name = "child_id")
    private String childId;


    /**
     * 子单的订单状态
     */
    @JSONField(name = "son_order_status")
    private String sonOrderStatus;

    /**
     * 订单id
     */
    @JSONField(name = "order_id")
    private String orderId;

    /**
     * SKU NO
     */
    @JSONField(name = "sku_code")
    private String skuCode;

    /**
     * 订单总金额
     */
    @JSONField(name = "total_product_amount")
    private AmountInfo totalProductAmount;


    /**
     * 产品单价
     */
    @JSONField(name = "product_unit_price")
    private AmountInfo productUnitPrice;

    /**
     * 物流金额（子订单没有运费，请忽略）
     */
    @JSONField(name = "logistics_amount")
    private AmountInfo logisticsAmount;






}
