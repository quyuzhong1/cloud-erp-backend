package com.erp.model.dmp.gyy;

import com.erp.model.dmp.gyy.bean.*;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    /**
     * 单据编号
     */
    @SerializedName("code")
    private String code;
    /**
     * 商品数量
     */
    @SerializedName("qty")
    private BigDecimal qty;
    /**
     * 订单金额
     * 商品明细amount总和+运费+优惠金额
     */
    @SerializedName("amount")
    private BigDecimal amount;
    /**
     * 支付金额
     */
    @SerializedName("payment")
    private BigDecimal payment;
    /**
     * 是否审核
     */
    @SerializedName("approve")
    private Boolean approve;
    /**
     * 是否货到付款
     */
    @SerializedName("cod")
    private Boolean cod;
    /**
     * 是否取消
     */
    @SerializedName("cancle")
    private Boolean cancle;
    /**
     * 内部便签
     */
    @SerializedName("msg")
    private String msg;
    /**
     * 身份证号
     */
    @SerializedName("vipIdCard")
    private String vipIdCard;
    /**
     * 电子邮箱
     */
    @SerializedName("vipEmail")
    private String vipEmail;
    /**
     * 真实姓名
     */
    @SerializedName("vipRealName")
    private String vipRealName;
    /**
     * 到账状态
     */
    @SerializedName("accountStatus")
    private String accountStatus;
    /**
     * 到账金额
     */
    @SerializedName("accountAmount")
    private BigDecimal accountAmount;
    /**
     * 配货状态 0:未配货 1:部分配货 2:全部配货
     */
    @SerializedName("assignState")
    private Integer assignState;
    /**
     * 退款状态 0:末退款1:退款成功2:退款中
     */
    @SerializedName("refund")
    private Integer refund;
    /**
     * 平台单号
     */
    @SerializedName("platform_code")
    private String platformCode;
    /**
     * 创建时间
     */
    @SerializedName("createtime")
    private String createtime;
    /**
     * 修改时间
     */
    @SerializedName("modifytime")
    private String modifytime;
    /**
     * 拍单时间
     */
    @SerializedName("dealtime")
    private String dealtime;
    /**
     * 支付时间
     */
    @SerializedName("paytime")
    private String paytime;
    /**
     * 店铺名称
     */
    @SerializedName("shop_name")
    private String shopName;
    /**
     * 店铺代码
     */
    @SerializedName("shop_code")
    private String shopCode;
    /**
     * 仓库名称
     */
    @SerializedName("warehouse_name")
    private String warehouseName;
    /**
     * 仓库代码
     */
    @SerializedName("warehouse_code")
    private String warehouseCode;
    /**
     * 快递公司名称
     */
    @SerializedName("express_name")
    private String expressName;
    /**
     * 快递公司代码
     */
    @SerializedName("express_code")
    private String expressCode;
    /**
     * 会员名称
     */
    @SerializedName("vip_name")
    private String vipName;
    /**
     * 会员代码
     */
    @SerializedName("vip_code")
    private String vipCode;
    /**
     * 收货人姓名
     */
    @SerializedName("receiver_name")
    private String receiverName;
    /**
     * 收货人电话
     */
    @SerializedName("receiver_phone")
    private String receiverPhone;
    /**
     * 收货人手机
     */
    @SerializedName("receiver_mobile")
    private String receiverMobile;
    /**
     * 收货人邮编
     */
    @SerializedName("receiver_zip")
    private String receiverZip;
    /**
     * 收货人地址
     */
    @SerializedName("receiver_address")
    private String receiverAddress;
    /**
     * 地区信息
     */
    @SerializedName("receiver_area")
    private String receiverArea;
    /**
     * 买家留言
     */
    @SerializedName("buyer_memo")
    private String buyerMemo;
    /**
     * 卖家留言
     */
    @SerializedName("seller_memo")
    private String sellerMemo;
    /**
     * 买家留言(延迟)
     */
    @SerializedName("seller_memo_late")
    private String sellerMemoLate;
    /**
     * 物流费用
     */
    @SerializedName("post_fee")
    private BigDecimal postFee;
    /**
     * 货到付款服务费
     */
    @SerializedName("cod_fee")
    private BigDecimal codFee;
    /**
     * 让利金额
     */
    @SerializedName("discount_fee")
    private BigDecimal discountFee;
    /**
     * 物流成本
     */
    @SerializedName("post_cost")
    private BigDecimal postCost;
    /**
     * 标准重量
     */
    @SerializedName("weight_origin")
    private BigDecimal weightOrigin;
    /**
     * 商品总金额
     */
    @SerializedName("payment_amount")
    private BigDecimal paymentAmount;
    /**
     * 发货状态 0:未发货 1:部分发货 2:全部发货
     */
    @SerializedName("delivery_state")
    private Integer deliveryState;
    /**
     * 订单类型 Sales-销售订单 Return-退货订单 Charge-费用订单 Invoice-补发票订单 Delivery-补发货订单
     */
    @SerializedName("order_type_name")
    private String orderTypeName;
    /**
     * 业务员
     */
    @SerializedName("business_man")
    private String businessMan;
    /**
     * 制单人
     */
    @SerializedName("create_name")
    private String createName;
    /**
     * 拦截信息
     */
    @SerializedName("hold_info")
    private String holdInfo;
    /**
     * 平台旗帜 0:无旗帜 1:红旗 2:黄旗 3:绿旗 4:蓝旗 5:紫旗
     */
    @SerializedName("platform_flag")
    private Integer platformFlag;
    /**
     * 异常信息
     */
    @SerializedName("error_msg")
    private String errorMsg;
    /**
     * 附加信息
     */
    @SerializedName("extend_memo")
    private String extendMemo;
    /**
     * 税额
     */
    @SerializedName("tax_amount")
    private BigDecimal taxAmount;
    /**
     * 优惠金额
     */
    @SerializedName("trade_order_status_info")
    private TradeOrderStatusInfoBean tradeOrderStatusInfo;
    /**
     * 审核时间
     */
    @SerializedName("approveDate")
    private String approveDate;
    /**
     * 到账时间
     */
    @SerializedName("accountDate")
    private String accountDate;
    /**
     * 订单标记代码
     */
    @SerializedName("trade_tag_code")
    private String tradeTagCode;
    /**
     * 订单标记名称
     */
    @SerializedName("trade_tag_name")
    private String tradeTagName;
    /**
     * 预计发货时间
     */
    @SerializedName("plan_delivery_date")
    private String planDeliveryDate;
    /**
     * 发票列表
     */
    @SerializedName("invoices")
    private Object invoices;
    /**
     * 平台交易状态
     */
    @SerializedName("platform_trading_state")
    private String platformTradingState;
    /**
     * 代发订单 0：非代发 1：代发
     */
    @SerializedName("substitut_order")
    private Integer substitutOrder;
    /**
     * 其他服务费
     */
    @SerializedName("other_service_fee")
    private BigDecimal otherServiceFee;
    /**
     * 分销商名称
     */
    @SerializedName("drp_tenant_name")
    private String drpTenantName;
    /**
     * 关税
     */
    @SerializedName("tariff_total")
    private BigDecimal tariffTotal;
    /**
     * 退款金额
     */
    @SerializedName("refund_fee")
    private BigDecimal refundFee;
    /**
     * 订单打印
     */
    @SerializedName("trade_print")
    private String tradePrint;
    /**
     * 币别代码
     */
    @SerializedName("currency_code")
    private String currencyCode;
    /**
     * 币别名称
     */
    @SerializedName("currency_name")
    private String currencyName;
    /**
     * 订单来源
     */
    @SerializedName("from_type_name")
    private String fromTypeName;
    /**
     * 物流匹配方式代码
     */
    @SerializedName("humen_express_code")
    private String humenExpressCode;
    /**
     * 物流匹配方式名称
     */
    @SerializedName("humen_express_name")
    private String humenExpressName;
    /**
     * 预售类型
     */
    @SerializedName("pre_sale")
    private Boolean preSale;
    /**
     * 子订单类型
     */
    @SerializedName("sub_type_name")
    private String subTypeName;
    /**
     * 是否分销商订单
     */
    @SerializedName("distribution_order")
    private Boolean distributionOrder;
    /**
     * 分销商物流费用
     */
    @SerializedName("distribution_post_fee")
    private String distributionPostFee;
    /**
     * 时效类型
     */
    @SerializedName("timeliness_type_name")
    private String timelinessTypeName;
    /**
     * 预计送达时间
     */
    @SerializedName("estimated_arrival_time")
    private String estimatedArrivalTime;
    /**
     * 门店/网店
     */
    @SerializedName("store_name")
    private String storeName;
    /**
     * bms仓库代码
     */
    @SerializedName("store_code")
    private String storeCode;
    /**
     * 是否退款 0:未退款 1:部分退款 2:全部退款
     */
    @SerializedName("refund_state")
    private Integer refundState;
    /**
     * 商品列表
     */
    @SerializedName("details")
    private List<DetailsBean> details;
    /**
     * 支付列表
     */
    @SerializedName("payments")
    private List<PaymentsBean> payments;
    /**
     * 发货列表
     */
    @SerializedName("deliverys")
    private List<DeliverysBean> deliverys;
    /**
     * 订单标签列表
     */
    @SerializedName("tags")
    private List<TagsBean> tags;
    /**
     * 内部便签列表
     */
    @SerializedName("messages")
    private List<Object> messages;

    /**
     * 数据下载状态
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    private Integer downloadStatus;

    /**
     * api编码，订单来源于历史订单还是当前订单接口区分
     */
    private String apiCode;
    /**
     * 清洗数据
     */
    private Boolean isClean;
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
                ", details=" + details +
                ", payments=" + payments +
                ", deliverys=" + deliverys +
                ", tags=" + tags +
                '}';
    }
}
