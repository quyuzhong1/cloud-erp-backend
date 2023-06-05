package com.erp.model.dmp.gyy;


import com.erp.model.dmp.gyy.bean.RefundDetailsBean;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class GyyRefundEntity {

    /**
     * create_date : 2022-11-23 16:24:10
     * modify_date : 2022-11-23 16:24:10
     * code : RMO551478381037
     * approve : false
     * cancel : false
     * note : null
     * reason : 与商家协商一致退款
     * agree_date : null
     * refund_phase : 买家确认收货前
     * account_id : null
     * account : null
     * status : SUCCESS
     * prime : null
     * shop_id : 320714301778
     * shop_code : 17
     * vip_code : AAH0JCKRAA5MQldmsLuTLlSM
     * payment_type_code : zhifubao
     * type_code : null
     * approveDate : null
     * platfrom_code : 3034892775716767224
     * refund_code : 192627291270762472
     * refund_type : deliveried_refund
     * agree_refuse : null
     * account_code : null
     * account_name : null
     * details : [{"qty":1,"discount":null,"note":null,"item_id":"313099370112","item_sku_id":null,"item_code":"1781","sku_code":null,"other_service_fee":0,"item_unit_name":null,"discount_fee":0,"origin_amount":"0","amount":"1.0000","origin_price":null,"price":"1.0000"}]
     * ag_status : 0
     * ag_err_msg : null
     * payment_type_name : null
     * buyer_account : null
     * order_status : null
     * business_man_name : null
     * create_name : null
     * store_name : null
     * return_order_code : null
     * amount : 1.0000
     */

    private String _id;
    /**
     * 创建时间
     */
    @SerializedName("create_date")
    private String createDate;
    /**
     * 修改时间
     */
    @SerializedName("modify_date")
    private String modifyDate;
    /**
     * 单据编号
     */
    @SerializedName("code")
    private String code;
    /**
     * 是否审核
     */
    @SerializedName("approve")
    private Boolean approve;
    /**
     * 是否取消
     */
    @SerializedName("cancel")
    private Boolean cancel;
    /**
     * 备注
     */
    @SerializedName("note")
    private String note;
    /**
     * 平台退款原因
     */
    @SerializedName("reason")
    private String reason;
    /**
     * 同意退款时间
     */
    @SerializedName("agree_date")
    private LocalDateTime agreeDate;
    /**
     * 退款阶段 买家确认收货前 买家确认收货后
     */
    @SerializedName("refund_phase")
    private String refundPhase;
    /**
     * 结算账户ID
     */
    @SerializedName("account_id")
    private String accountId;
    /**
     * 结算账户账号
     */
    @SerializedName("account")
    private String account;
    /**
     * 平台状态
     */
    @SerializedName("status")
    private String status;
    /**
     * 专享服务
     */
    @SerializedName("prime")
    private String prime;
    /**
     * 店铺ID
     */
    @SerializedName("shop_id")
    private String shopId;
    /**
     * 店铺编码
     */
    @SerializedName("shop_code")
    private String shopCode;
    /**
     * 会员代码
     */
    @SerializedName("vip_code")
    private String vipCode;
    /**
     * 退款支付方式代码
     */
    @SerializedName("payment_type_code")
    private String paymentTypeCode;
    /**
     * 单据类型
     */
    @SerializedName("type_code")
    private String typeCode;
    /**
     * 审核时间
     */
    @SerializedName("approveDate")
    private String approveDate;
    /**
     * 平台订单号
     */
    @SerializedName("platfrom_code")
    private String platfromCode;
    /**
     * 平台退款单号
     */
    @SerializedName("refund_code")
    private String refundCode;
    /**
     * 退款种类 refund:仅退款 return:退货退款 deliveried_refund:发货后仅退款
     */
    @SerializedName("refund_type")
    private String refundType;
    /**
     * 同意/拒绝 1:同意退款 2:拒绝退款
     */
    @SerializedName("agree_refuse")
    private Integer agreeRefuse;
    /**
     * 结算账户代码
     */
    @SerializedName("account_code")
    private String accountCode;
    /**
     * 结算账户名称
     */
    @SerializedName("account_name")
    private String accountName;
    /**
     * 平台返回信息
     */
    @SerializedName("ag_status")
    private Integer agStatus;
    /**
     * 异常信息
     */
    @SerializedName("ag_err_msg")
    private String agErrMsg;
    /**
     * 退款方式名称
     */
    @SerializedName("payment_type_name")
    private String paymentTypeName;
    /**
     * 买家收款账号
     */
    @SerializedName("buyer_account")
    private String buyerAccount;
    /**
     * 系统订单状态
     */
    @SerializedName("order_status")
    private String orderStatus;
    /**
     * 业务员名称
     */
    @SerializedName("business_man_name")
    private String businessManName;
    /**
     * 制单人
     */
    @SerializedName("create_name")
    private String createName;
    /**
     * 门店名称
     */
    @SerializedName("store_name")
    private String storeName;
    /**
     * 退换货单号
     */
    @SerializedName("return_order_code")
    private String returnOrderCode;
    /**
     * 实际退款金额
     */
    @SerializedName("amount")
    private BigDecimal amount;
    @SerializedName("details")
    private List<RefundDetailsBean> details;
    /**
     * 清洗数据
     */
    private Boolean isClean;
    @Override
    public String toString() {
        return "GyyRefundEntity{" +
                "createDate='" + createDate + '\'' +
                ", modifyDate='" + modifyDate + '\'' +
                ", code='" + code + '\'' +
                ", approve=" + approve +
                ", cancel=" + cancel +
                ", note='" + note + '\'' +
                ", reason='" + reason + '\'' +
                ", agreeDate=" + agreeDate +
                ", refundPhase='" + refundPhase + '\'' +
                ", accountId='" + accountId + '\'' +
                ", account='" + account + '\'' +
                ", status='" + status + '\'' +
                ", prime='" + prime + '\'' +
                ", shopId='" + shopId + '\'' +
                ", shopCode='" + shopCode + '\'' +
                ", vipCode='" + vipCode + '\'' +
                ", paymentTypeCode='" + paymentTypeCode + '\'' +
                ", typeCode='" + typeCode + '\'' +
                ", approveDate='" + approveDate + '\'' +
                ", platfromCode='" + platfromCode + '\'' +
                ", refundCode='" + refundCode + '\'' +
                ", refundType='" + refundType + '\'' +
                ", agreeRefuse=" + agreeRefuse +
                ", accountCode='" + accountCode + '\'' +
                ", accountName='" + accountName + '\'' +
                ", agStatus=" + agStatus +
                ", agErrMsg='" + agErrMsg + '\'' +
                ", paymentTypeName='" + paymentTypeName + '\'' +
                ", buyerAccount='" + buyerAccount + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", businessManName='" + businessManName + '\'' +
                ", createName='" + createName + '\'' +
                ", storeName='" + storeName + '\'' +
                ", returnOrderCode='" + returnOrderCode + '\'' +
                ", amount=" + amount +
                ", details=" + details +
                '}';
    }
}
