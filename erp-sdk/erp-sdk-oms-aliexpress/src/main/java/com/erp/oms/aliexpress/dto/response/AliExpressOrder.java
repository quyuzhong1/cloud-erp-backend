package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-29 10:27
 */
@Data
public class AliExpressOrder implements Serializable {

    /**
     * 订单创建时间
     */
    @SerializedName("gmt_create")
    private String gmtCreate;

    /**
     * 冻结状态。(NO_FROZEN:未冻结；IN_FROZEN:处于冻结状态)
     */
    @SerializedName("frozen_status")
    private String frozenStatus;

    /**
     * 订单状态
     */
    @SerializedName("order_status")
    private String orderStatus;

    /**
     * 当前状态的剩余时间（负数表示超时时间）
     */
    @SerializedName("timeout_left_time")
    private Long timeoutLeftTime;

    /**
     * 买家登录id
     */
    @SerializedName("buyer_login_id")
    private String buyerLoginId;

    /**
     * 卖家登录id
     */
    @SerializedName("seller_login_id")
    private String sellerLoginId;


    /**
     * 订单完成原因
     */
    @SerializedName("end_reason")
    private String endReason;


    /**
     * 支付时间
     */
    @SerializedName("gmt_pay_time")
    private String gmtPayTime;

    /**
     * 资金状态 status (NOT_PAY; PAY_SUCCESS; WAIT_SELLER_CHECK)
     */
    @SerializedName("fund_status")
    private String fundStatus;

    /**
     * 订单类型。（AE_COMMON:普通类型,AE_TRIAL:试用类型;AE_RECHARGE:充值订单
     */
    @SerializedName("biz_type")
    private String bizType;

    /**
     * 问题状态  (NO_ISSUE; IN_ISSUE; END_ISSUE)
     */
    @SerializedName("issue_status")
    private String issueStatus;

    /**
     * 买方全名
     */
    @SerializedName("buyer_signer_fullname")
    private String buyerSignerFullname;

    /**
     * 手续费
     */
    @SerializedName("escrow_fee")
    private AmountInfo escrowFee;

    /**
     * 上次订单更新时间
     */
    @SerializedName("gmt_update")
    private String gmtUpdate;

    /**
     * 支付类型：mig：信用卡支付采用人民币通道；
     * migs：102万事达卡支付走人民币通道；
     * migs101：Visa Pay，走人民币通道；
     * pp101：贝宝；mb：MoneyBooker 频道；
     * tt101：银行转账付款；wu101：西联支付；
     * wp101：Visa支付美元通道；wp102：
     * 万事达美元支付通道；qw101：QIWI支付；
     * cybs101：Visa走CYBS通道的支付；
     * cybs102：万事达卡支付CYBS频道；
     * wm101：WebMoney支付；
     * ebanx101：巴西Beloto支付；
     */
    @SerializedName("payment_type")
    private String paymentType;


    /**
     * 是否申请贷款
     */
    @SerializedName("has_request_loan")
    private Boolean hasRequestLoan;

    /**
     * 卖家操作员登录id
     */
    @SerializedName("seller_operator_login_id")
    private String sellerOperatorLoginId;


    /**
     * 电话
     */
    @SerializedName("phone")
    private String phone;

    /**
     * 物流托管费率
     */
    @SerializedName("logisitcs_escrow_fee_rate")
    private String logisitcsEscrowFeeRate;

    /**
     * 订单id
     */
    @SerializedName("order_id")
    private String orderId;

    /**
     * 卖家全名
     */
    @SerializedName("seller_signer_fullname")
    private String sellerSignerFullname;


    /**
     * 支付金額
     */
    @SerializedName("pay_amount")
    private AmountInfo payAmount;


    /**
     * 物流状态
     * （
     * WAIT_SELLER_SEND_GOODS:等待卖家发货;
     * SELLER_SEND_PART_GOODS:卖家部分发货;
     * SELLER_SEND_GOODS:卖家已发货;
     * BUYER_ACCEPT_GOODS:买家已确认收货;
     * NO_LOGISTICS:没有物流流转信息
     * ）
     */
    @SerializedName("logistics_status")
    private String logisticsStatus;

    /**
     * 产品明细
     */
    private AliExpressOrderDetail detail;


    /**
     * 是否有发货单下载
     */
    public boolean canDownloadDelivery(){
        if (StringUtils.isBlank(this.getLogisticsStatus())) {
            return false;
        }
        return "SELLER_SEND_PART_GOODS".equalsIgnoreCase(this.getLogisticsStatus())
                || "SELLER_SEND_GOODS".equalsIgnoreCase(this.getLogisticsStatus())
                || "BUYER_ACCEPT_GOODS".equalsIgnoreCase(this.getLogisticsStatus());
    }


    /**
     * 转单据状态
     *
     * @author yl
     * @date 2023-11-29 16:10
     */
    public String convertBillStatus(Boolean isPlatformWarehouseOrder, Object logisticInfoListObj) {
        String orderStatus = this.getOrderStatus();
        if (StringUtils.isBlank(orderStatus)) {
            return SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
        }
        //冻结
        if (isPlatformWarehouseOrder) {
            if ("IN_CANCEL".equals(orderStatus)
                    || "RISK_CONTROL".equals(orderStatus)) {

                return SoB2cBillStatusEnum.ENUM_FROZEN.getCode();
            }
        }else{
            if("IN_CANCEL".equals(orderStatus)
                    || "RISK_CONTROL".equals(orderStatus)
                    ||"IN_FROZEN".equals(orderStatus)){

//                return SoB2cBillStatusEnum.ENUM_FROZEN.getCode();
                return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
            }

        }
        // 部分发货
        if ("SELLER_PART_SEND_GOODS".equalsIgnoreCase(orderStatus)){
            return SoB2cBillStatusEnum.ENUM_PARTIAL_SHIPPED.getCode();
        }

        //平台仓订单
        if(isPlatformWarehouseOrder){
            if ("PLACE_ORDER_SUCCESS".equals(orderStatus)
                    || "PAYMENT_PROCESSING".equals(orderStatus)
                    || "IN_FROZEN".equals(orderStatus)) {
                return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
            }

            if ("WAIT_SELLER_SEND_GOODS".equals(orderStatus)) {
                return SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
            }
        }else{
            if ("PLACE_ORDER_SUCCESS".equals(orderStatus)
                    || "WAIT_SELLER_SEND_GOODS".equals(orderStatus)
                    || "PAYMENT_PROCESSING".equals(orderStatus)
                   ) {
                return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
            }
        }
        //待发货
        if ("SELLER_PART_SEND_GOODS".equals(orderStatus)) {
            return SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
        }

        //已发货
        if ("WAIT_BUYER_ACCEPT_GOODS".equals(orderStatus)
                || "FUND_PROCESSING".equals(orderStatus)
                || "IN_ISSUE".equals(orderStatus)
                || "WAIT_SELLER_EXAMINE_MONEY".equals(orderStatus)
                // 完结已发货
                || finishShipped(logisticInfoListObj)
        ) {
            return SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        }


        // 待配货
        return SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode();
    }

    /**
     * 完结已发货
     */
    public boolean finishShipped(Object logisticInfoListObj) {
        if (null == logisticInfoListObj){
            return false;
        }
        JSONArray logisticInfoArray = JSON.parseArray(JSON.toJSONString(logisticInfoListObj));
        if (CollectionUtils.isEmpty(logisticInfoArray)){
            return false;
        }
        // 存在签收信息
        boolean existReceive = logisticInfoArray.stream()
                .map(e -> JSON.parseObject(JSON.toJSONString(e)))
                .anyMatch(e -> "received".equalsIgnoreCase(e.getString("receive_status")));
        return "FINISH".equalsIgnoreCase(orderStatus) && existReceive;
    }

    /**
     * 转付款状态
     *
     * @author yl
     * @date 2023-11-29 16:10
     */
    public String convertPayStatus() {
        String orderStatus = this.getOrderStatus();
        if (StringUtils.isBlank(orderStatus)
                || "PLACE_ORDER_SUCCESS".equals(orderStatus)
                || "PAYMENT_PROCESSING".equals(orderStatus)
        ) {
            return SoB2cPayStatusEnum.ENUM_PAYMENT.getCode();
        } else {
            return SoB2cPayStatusEnum.ENUM_PAID.getCode();
        }

    }


    public String convertApproveStatus(Boolean isPlatformWarehouseOrder, Object logisticInfoListObj) {
        String orderStatus = this.getOrderStatus();
        if (StringUtils.isBlank(orderStatus)) {
            return ApproveStatusEnum.WAIT_SUBMIT.getCode();
        }
        // 完结已发货(自动已审核)
        if (finishShipped(logisticInfoListObj)){
            return ApproveStatusEnum.APPROVE.getCode();
        }
        // 订单取消=待提交
        if(convertNewCancel(logisticInfoListObj)){
            return ApproveStatusEnum.WAIT_SUBMIT.getCode();
        }

        if (isPlatformWarehouseOrder) {
            if ("PLACE_ORDER_SUCCESS".equals(orderStatus)) {
                return ApproveStatusEnum.WAIT_SUBMIT.getCode();
            }
            return ApproveStatusEnum.APPROVE.getCode();
        } else {
            //自发货
            if ("PLACE_ORDER_SUCCESS".equals(orderStatus)
                    || "WAIT_SELLER_SEND_GOODS".equals(orderStatus)
                    || "PAYMENT_PROCESSING".equals(orderStatus)
                    || "RISK_CONTROL".equals(orderStatus)
                    || "IN_FROZEN".equals(orderStatus)
            ) {
                return ApproveStatusEnum.WAIT_SUBMIT.getCode();
            }
            if ("SELLER_PART_SEND_GOODS".equals(orderStatus)
                    || "WAIT_BUYER_ACCEPT_GOODS".equals(orderStatus)
                    || "FUND_PROCESSING".equals(orderStatus)
                    || "IN_ISSUE".equals(orderStatus)
                    || "WAIT_SELLER_EXAMINE_MONEY".equals(orderStatus)) {
                return ApproveStatusEnum.APPROVE.getCode();

            }

        }
        return ApproveStatusEnum.WAIT_SUBMIT.getCode();
    }

    /**
     * 速卖通转换平台取消状态
     *
     * end_issue结束问题
     * trade_close交易关闭
     * buyer_confirm_goods买家确认货物 ()
     * buyer_confirm_goods_timeout买家确认货物超时
     * pay_timeout支付超时
     * buyer_cancel_order买家取消订单
     * risk_closed风险已关闭
     * suspicious_trade可疑交易
     * confirm_payamount_timeout确认付款金额超时
     * reject_payamount拒绝付款金额
     * send_goods_timeout发送货物超时
     * buyer_cancel_notpay_order买家取消未支付订单
     * buyer_cancel_order_in_risk买家取消风控订单
     * security_close安全关闭
     */
    @Deprecated
    public boolean convertCancel() {
        // 冻结中视为取消走拦截逻辑或初始化作废
        if("IN_CANCEL".equals(orderStatus)
                || "RISK_CONTROL".equals(orderStatus)
                ||"IN_FROZEN".equals(orderStatus)){
            return true;
        }

        if (!"FINISH".equalsIgnoreCase(this.orderStatus)){
            // 非完结
            return false;
        }
        if (StringUtils.isBlank(this.endReason)){
            // 无完结原因（平台自动取消）
            return true;
        }
        // 非买家确认货物 和 买家确认货物超时 都视为取消
        return !"buyer_confirm_goods".equalsIgnoreCase(this.endReason)
                && !"buyer_confirm_goods_timeout".equalsIgnoreCase(this.endReason)
                ;
    }

    /**
     * 冻结中
     */
    public boolean convertFrozen(){
        // 冻结中视为取消走拦截逻辑或初始化作废
        return "IN_CANCEL".equals(orderStatus)
                || "RISK_CONTROL".equals(orderStatus)
                || "IN_FROZEN".equals(orderStatus);
    }

    /**
     * 判断速卖通订单是否是售前取消
     * logisticInfoListObj
     */
    public boolean convertNewCancel(Object logisticInfoListObj) {
        // 冻结中视为取消走拦截逻辑或初始化作废
        if("IN_CANCEL".equals(orderStatus)
                || "RISK_CONTROL".equals(orderStatus)
                ||"IN_FROZEN".equals(orderStatus)){
            return true;
        }

        if (!"FINISH".equalsIgnoreCase(this.orderStatus)){
            // 非完结
            return false;
        }
        // 无物流信息
        if (null == logisticInfoListObj){
            return true;
        }
        JSONArray logisticInfoArray = JSON.parseArray(JSON.toJSONString(logisticInfoListObj));
        if (CollectionUtils.isEmpty(logisticInfoArray)){
            return true;
        }
        // 存在签收信息
        boolean existReceive = logisticInfoArray.stream()
                .map(e -> JSON.parseObject(JSON.toJSONString(e)))
                .anyMatch(e -> ("received".equalsIgnoreCase(e.getString("receive_status")) || "not_received".equalsIgnoreCase(e.getString("receive_status"))));
        return !existReceive;
    }
}
