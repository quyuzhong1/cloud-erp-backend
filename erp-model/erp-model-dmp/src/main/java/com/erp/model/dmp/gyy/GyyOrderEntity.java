package com.erp.model.dmp.gyy;

import com.erp.model.dmp.gyy.bean.*;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class GyyOrderEntity {

    /**
     * code : SO546015421746
     * qty : 1.0
     * amount : 99.0
     * payment : 85.67
     * approve : true
     * cod : false
     * cancle : false
     * msg : null
     * vipIdCard : null
     * vipEmail : null
     * vipRealName : null
     * accountStatus : 未到账
     * accountAmount : 0.0
     * assignState : 2
     * refund : 1
     * platform_code : 1733215692079667898
     * createtime : 2022-11-10 20:16:49
     * modifytime : 2022-11-16 15:43:22
     * dealtime : 2022-11-10 20:12:09
     * paytime : 2022-11-10 20:12:12
     * shop_name : 天猫-Ulanzi优篮子专卖店
     * shop_code : 11
     * warehouse_name : B2C天猫京东仓
     * warehouse_code : CK062
     * express_name : 中通-菜鸟面单
     * express_code : ZTCN
     * vip_name : 笨**
     * vip_code : AAHzJCKRAA5MQldmsLsJXufS
     * receiver_name : 吕**
     * receiver_phone :
     * receiver_mobile : *******9727
     * receiver_zip : 453000
     * receiver_address : 河南省 新乡市 牧野区 牧*乡**城**号**单元
     * receiver_area : 河南省-新乡市-牧野区
     * buyer_memo : null
     * seller_memo : null
     * seller_memo_late : null
     * post_fee : 0.0
     * cod_fee : 0.0
     * discount_fee : 13.33
     * post_cost : 0.0
     * weight_origin : 150.0
     * payment_amount : 99.0
     * delivery_state : 2
     * order_type_name : 销售订单
     * business_man : 吴晓锋
     * create_name : 自动下载
     * hold_info : null
     * platform_flag : 0
     * error_msg : 退款订单
     * extend_memo : null
     * tax_amount : 0.0
     * trade_order_status_info : {"id":546015421746,"createDate":1668082609000,"modifyDate":1668584602000,"version":7,"tenantId":307494129406,"approve":true,"approveName":"吴锦辉","approveDate":1668130816000,"cancel":false,"cancelName":null,"cancelDate":null,"delivery":true,"deliveryDate":1668145716000,"platformDelivery":true,"platformDeliveryDate":1668145716000,"assignState":2,"deliveryState":2}
     * approveDate : 2022-11-11 09:40:16
     * accountDate :
     * trade_tag_code :
     * trade_tag_name :
     * plan_delivery_date :
     * details : [{"oid":"1733215692079667898","qty":1,"price":99,"amount":99,"refund":2,"discount":0.5,"note":"狂欢价 2022年天猫双11狂欢日跨店满减 ","cancel":false,"del":false,"weight":150,"item_code":"3021","item_name":"ULANZI LT010 Magsafe翻折补光灯 - M-light","item_simple_name":"ULANZI LT010 Magsafe翻折补光灯","sku_name":null,"sku_code":null,"post_fee":0,"discount_fee":13.33,"amount_after":85.67,"origin_price":198,"origin_amount":198,"platform_item_name":"3045||【狂欢价】网红推荐Ulanzi优篮子手机Magsafe磁吸补光灯适用iphone14苹果12/13系列直播拍照打光摄[680722100260]","platform_sku_name":"3021||黑色[5051148554699]","delivering_qty":1,"delivered_qty":1,"tax_rate":0,"tax_amount":0,"sku_note":null,"other_service_fee":0,"is_gift":false,"tariff_amount":0,"item_unit_name":null,"tax_no":null,"exchange_rate":0,"cost_price":0,"cost_price_total":null,"plat_discount_amount":0,"distribution_post_fee":0,"gift_source_view":null,"saleable_qty":0,"pickable_qty":0,"warehouse_name":null,"stock_status_name":null,"estimate_arrived_date":null,"plan_delivery_date":null,"maintain_num":null,"assign_state":null,"delivery_state":null,"pre_sale":null,"warehouse_code":null,"store_code":null,"bms_name":null,"minus_stock":null,"bms_status":0}]
     * payments : [{"payment":85.67,"payCode":"2022111022001160975737046693","account":"182****9727","pay_type_name":"支付宝","paytime":"2022-11-10 20:12:12"}]
     * invoices : null
     * deliverys : [{"delivery":true,"code":"SDO546455586734","printExpress":true,"printDeliveryList":false,"scan":true,"weight":false,"warehouse_name":"B2C天猫京东仓","warehouse_code":"CK062","express_name":"中通-菜鸟面单","express_code":"ZTCN","mail_no":"78632828292719"}]
     * tags : [{"tag_code":"refund","tag_name":"退款","tag_note":""}]
     * messages : []
     * platform_trading_state : 卖家已发货
     * substitut_order : 0
     * other_service_fee : 0.0
     * drp_tenant_name : null
     * tariff_total : 0.0
     * refund_fee : 0.0
     * trade_print : null
     * currency_code : null
     * currency_name : null
     * from_type_name : 天猫
     * humen_express_code : null
     * humen_express_name : null
     * pre_sale : false
     * sub_type_name : null
     * distribution_order : false
     * distribution_post_fee : null
     * timeliness_type_name : null
     * estimated_arrival_time : null
     * store_name : null
     * store_code : null
     * refund_state : 0
     * distribution_channel : null
     */
    private String _id;
    @SerializedName("code")
    private String code;
    @SerializedName("qty")
    private BigDecimal qty;
    @SerializedName("amount")
    private BigDecimal amount;
    @SerializedName("payment")
    private BigDecimal payment;
    @SerializedName("approve")
    private Boolean approve;
    @SerializedName("cod")
    private Boolean cod;
    @SerializedName("cancle")
    private Boolean cancle;
    @SerializedName("msg")
    private String msg;
    @SerializedName("vipIdCard")
    private String vipIdCard;
    @SerializedName("vipEmail")
    private String vipEmail;
    @SerializedName("vipRealName")
    private String vipRealName;
    @SerializedName("accountStatus")
    private String accountStatus;
    @SerializedName("accountAmount")
    private BigDecimal accountAmount;
    @SerializedName("assignState")
    private Integer assignState;
    @SerializedName("refund")
    private Integer refund;
    @SerializedName("platform_code")
    private String platformCode;
    @SerializedName("createtime")
    private String createtime;
    @SerializedName("modifytime")
    private String modifytime;
    @SerializedName("dealtime")
    private String dealtime;
    @SerializedName("paytime")
    private String paytime;
    @SerializedName("shop_name")
    private String shopName;
    @SerializedName("shop_code")
    private String shopCode;
    @SerializedName("warehouse_name")
    private String warehouseName;
    @SerializedName("warehouse_code")
    private String warehouseCode;
    @SerializedName("express_name")
    private String expressName;
    @SerializedName("express_code")
    private String expressCode;
    @SerializedName("vip_name")
    private String vipName;
    @SerializedName("vip_code")
    private String vipCode;
    @SerializedName("receiver_name")
    private String receiverName;
    @SerializedName("receiver_phone")
    private String receiverPhone;
    @SerializedName("receiver_mobile")
    private String receiverMobile;
    @SerializedName("receiver_zip")
    private String receiverZip;
    @SerializedName("receiver_address")
    private String receiverAddress;
    @SerializedName("receiver_area")
    private String receiverArea;
    @SerializedName("buyer_memo")
    private String buyerMemo;
    @SerializedName("seller_memo")
    private String sellerMemo;
    @SerializedName("seller_memo_late")
    private String sellerMemoLate;
    @SerializedName("post_fee")
    private BigDecimal postFee;
    @SerializedName("cod_fee")
    private BigDecimal codFee;
    @SerializedName("discount_fee")
    private BigDecimal discountFee;
    @SerializedName("post_cost")
    private BigDecimal postCost;
    @SerializedName("weight_origin")
    private BigDecimal weightOrigin;
    @SerializedName("payment_amount")
    private BigDecimal paymentAmount;
    @SerializedName("delivery_state")
    private Integer deliveryState;
    @SerializedName("order_type_name")
    private String orderTypeName;
    @SerializedName("business_man")
    private String businessMan;
    @SerializedName("create_name")
    private String createName;
    @SerializedName("hold_info")
    private String holdInfo;
    @SerializedName("platform_flag")
    private Integer platformFlag;
    @SerializedName("error_msg")
    private String errorMsg;
    @SerializedName("extend_memo")
    private String extendMemo;
    @SerializedName("tax_amount")
    private BigDecimal taxAmount;
    @SerializedName("trade_order_status_info")
    private TradeOrderStatusInfoBean tradeOrderStatusInfo;
    @SerializedName("approveDate")
    private String approveDate;
    @SerializedName("accountDate")
    private String accountDate;
    @SerializedName("trade_tag_code")
    private String tradeTagCode;
    @SerializedName("trade_tag_name")
    private String tradeTagName;
    @SerializedName("plan_delivery_date")
    private String planDeliveryDate;
    @SerializedName("invoices")
    private String invoices;
    @SerializedName("platform_trading_state")
    private String platformTradingState;
    @SerializedName("substitut_order")
    private Integer substitutOrder;
    @SerializedName("other_service_fee")
    private BigDecimal otherServiceFee;
    @SerializedName("drp_tenant_name")
    private String drpTenantName;
    @SerializedName("tariff_total")
    private BigDecimal tariffTotal;
    @SerializedName("refund_fee")
    private BigDecimal refundFee;
    @SerializedName("trade_print")
    private String tradePrint;
    @SerializedName("currency_code")
    private String currencyCode;
    @SerializedName("currency_name")
    private String currencyName;
    @SerializedName("from_type_name")
    private String fromTypeName;
    @SerializedName("humen_express_code")
    private String humenExpressCode;
    @SerializedName("humen_express_name")
    private String humenExpressName;
    @SerializedName("pre_sale")
    private Boolean preSale;
    @SerializedName("sub_type_name")
    private String subTypeName;
    @SerializedName("distribution_order")
    private Boolean distributionOrder;
    @SerializedName("distribution_post_fee")
    private String distributionPostFee;
    @SerializedName("timeliness_type_name")
    private String timelinessTypeName;
    @SerializedName("estimated_arrival_time")
    private String estimatedArrivalTime;
    @SerializedName("store_name")
    private String storeName;
    @SerializedName("store_code")
    private String storeCode;
    @SerializedName("refund_state")
    private Integer refundState;
    @SerializedName("distribution_channel")
    private String distributionChannel;
    @SerializedName("details")
    private List<DetailsBean> details;
    @SerializedName("payments")
    private List<PaymentsBean> payments;
    @SerializedName("deliverys")
    private List<DeliverysBean> deliverys;
    @SerializedName("tags")
    private List<TagsBean> tags;
    @SerializedName("messages")
    private List<Object> messages;

    @Override
    public String toString() {
        return "GyyOrderEntity{" +
                ", code='" + code + '\'' +
                ", qty=" + qty +
                ", amount=" + amount +
                ", payment=" + payment +
                ", approve=" + approve +
                ", cod=" + cod +
                ", cancle=" + cancle +
                ", msg='" + msg + '\'' +
                ", vipIdCard='" + vipIdCard + '\'' +
                ", vipEmail='" + vipEmail + '\'' +
                ", vipRealName='" + vipRealName + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", accountAmount=" + accountAmount +
                ", assignState=" + assignState +
                ", refund=" + refund +
                ", platformCode='" + platformCode + '\'' +
                ", createtime='" + createtime + '\'' +
                ", modifytime='" + modifytime + '\'' +
                ", dealtime='" + dealtime + '\'' +
                ", paytime='" + paytime + '\'' +
                ", shopName='" + shopName + '\'' +
                ", shopCode='" + shopCode + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", warehouseCode='" + warehouseCode + '\'' +
                ", expressName='" + expressName + '\'' +
                ", expressCode='" + expressCode + '\'' +
                ", vipName='" + vipName + '\'' +
                ", vipCode='" + vipCode + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", receiverPhone='" + receiverPhone + '\'' +
                ", receiverMobile='" + receiverMobile + '\'' +
                ", receiverZip='" + receiverZip + '\'' +
                ", receiverAddress='" + receiverAddress + '\'' +
                ", receiverArea='" + receiverArea + '\'' +
                ", buyerMemo='" + buyerMemo + '\'' +
                ", sellerMemo='" + sellerMemo + '\'' +
                ", sellerMemoLate='" + sellerMemoLate + '\'' +
                ", postFee=" + postFee +
                ", codFee=" + codFee +
                ", discountFee=" + discountFee +
                ", postCost=" + postCost +
                ", weightOrigin=" + weightOrigin +
                ", paymentAmount=" + paymentAmount +
                ", deliveryState=" + deliveryState +
                ", orderTypeName='" + orderTypeName + '\'' +
                ", businessMan='" + businessMan + '\'' +
                ", createName='" + createName + '\'' +
                ", holdInfo='" + holdInfo + '\'' +
                ", platformFlag=" + platformFlag +
                ", errorMsg='" + errorMsg + '\'' +
                ", extendMemo='" + extendMemo + '\'' +
                ", taxAmount=" + taxAmount +
                ", tradeOrderStatusInfo=" + tradeOrderStatusInfo +
                ", approveDate='" + approveDate + '\'' +
                ", accountDate='" + accountDate + '\'' +
                ", tradeTagCode='" + tradeTagCode + '\'' +
                ", tradeTagName='" + tradeTagName + '\'' +
                ", planDeliveryDate='" + planDeliveryDate + '\'' +
                ", invoices='" + invoices + '\'' +
                ", platformTradingState='" + platformTradingState + '\'' +
                ", substitutOrder=" + substitutOrder +
                ", otherServiceFee=" + otherServiceFee +
                ", drpTenantName='" + drpTenantName + '\'' +
                ", tariffTotal=" + tariffTotal +
                ", refundFee=" + refundFee +
                ", tradePrint='" + tradePrint + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", currencyName='" + currencyName + '\'' +
                ", fromTypeName='" + fromTypeName + '\'' +
                ", humenExpressCode='" + humenExpressCode + '\'' +
                ", humenExpressName='" + humenExpressName + '\'' +
                ", preSale=" + preSale +
                ", subTypeName='" + subTypeName + '\'' +
                ", distributionOrder=" + distributionOrder +
                ", distributionPostFee='" + distributionPostFee + '\'' +
                ", timelinessTypeName='" + timelinessTypeName + '\'' +
                ", estimatedArrivalTime='" + estimatedArrivalTime + '\'' +
                ", storeName='" + storeName + '\'' +
                ", storeCode='" + storeCode + '\'' +
                ", refundState=" + refundState +
                ", distributionChannel='" + distributionChannel + '\'' +
                ", details=" + details +
                ", payments=" + payments +
                ", deliverys=" + deliverys +
                ", tags=" + tags +
                '}';
    }
}
