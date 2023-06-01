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

    @SerializedName("create_date")
    private String createDate;
    @SerializedName("modify_date")
    private String modifyDate;
    @SerializedName("code")
    private String code;
    @SerializedName("approve")
    private Boolean approve;
    @SerializedName("cancel")
    private Boolean cancel;
    @SerializedName("note")
    private String note;
    @SerializedName("reason")
    private String reason;
    @SerializedName("agree_date")
    private LocalDateTime agreeDate;
    @SerializedName("refund_phase")
    private String refundPhase;
    @SerializedName("account_id")
    private String accountId;
    @SerializedName("account")
    private String account;
    @SerializedName("status")
    private String status;
    @SerializedName("prime")
    private String prime;
    @SerializedName("shop_id")
    private String shopId;
    @SerializedName("shop_code")
    private String shopCode;
    @SerializedName("vip_code")
    private String vipCode;
    @SerializedName("payment_type_code")
    private String paymentTypeCode;
    @SerializedName("type_code")
    private String typeCode;
    @SerializedName("approveDate")
    private String approveDate;
    @SerializedName("platfrom_code")
    private String platfromCode;
    @SerializedName("refund_code")
    private String refundCode;
    @SerializedName("refund_type")
    private String refundType;
    @SerializedName("agree_refuse")
    private Integer agreeRefuse;
    @SerializedName("account_code")
    private String accountCode;
    @SerializedName("account_name")
    private String accountName;
    @SerializedName("ag_status")
    private Integer agStatus;
    @SerializedName("ag_err_msg")
    private String agErrMsg;
    @SerializedName("payment_type_name")
    private String paymentTypeName;
    @SerializedName("buyer_account")
    private String buyerAccount;
    @SerializedName("order_status")
    private String orderStatus;
    @SerializedName("business_man_name")
    private String businessManName;
    @SerializedName("create_name")
    private String createName;
    @SerializedName("store_name")
    private String storeName;
    @SerializedName("return_order_code")
    private String returnOrderCode;
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
