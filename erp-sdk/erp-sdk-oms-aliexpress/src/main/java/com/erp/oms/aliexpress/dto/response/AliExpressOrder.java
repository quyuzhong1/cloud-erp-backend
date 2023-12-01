package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-29 10:27
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AliExpressOrder  implements Serializable {

    /**
     * 订单创建时间
     */
    @JSONField(name = "gmt_create")
    private String  gmtCreate;

    /**
     * 冻结状态。(NO_FROZEN:未冻结；IN_FROZEN:处于冻结状态)
     */
    @JSONField(name = "frozen_status")
    private String frozenStatus;

    /**
     * 订单状态
     */
    @JSONField(name = "order_status")
    private String orderStatus;

    /**
     * 当前状态的剩余时间（负数表示超时时间）
     */
    @JSONField(name = "timeout_left_time")
    private Long timeoutLeftTime;

    /**
     * 买家登录id
     */
    @JSONField(name = "buyer_login_id")
    private String buyerLoginId;

    /**
     * 卖家登录id
     */
    @JSONField(name = "seller_login_id")
    private String sellerLoginId;


    /**
     * 订单完成原因
     */
    @JSONField(name = "end_reason")
    private String endReason;


    /**
     * 支付时间
     */
    @JSONField(name = "gmt_pay_time")
    private String gmtPayTime;

    /**
     * 资金状态 status (NOT_PAY; PAY_SUCCESS; WAIT_SELLER_CHECK)
     */
    @JSONField(name = "fund_status")
    private String fundStatus;

    /**
     * 订单类型。（AE_COMMON:普通类型,AE_TRIAL:试用类型;AE_RECHARGE:充值订单
     */
    @JSONField(name = "biz_type")
    private String bizType;

    /**
     * 问题状态  (NO_ISSUE; IN_ISSUE; END_ISSUE)
     */
    @JSONField(name = "issue_status")
    private String issueStatus;

    /**
     * 买方全名
     */
    @JSONField(name = "buyer_signer_fullname")
    private String buyerSignerFullname;

    /**
     * 上次订单更新时间
     */
    @JSONField(name = "gmt_update")
    private String gmtUpdate;

    /**
     *支付类型：mig：信用卡支付采用人民币通道；
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
    @JSONField(name = "payment_type")
    private String paymentType;


    /**
     *是否申请贷款
     */
    @JSONField(name = "has_request_loan")
    private Boolean hasRequestLoan;

    /**
     *卖家操作员登录id
     */
    @JSONField(name = "seller_operator_login_id")
    private String sellerOperatorLoginId;


    /**
     *电话
     */
    @JSONField(name = "phone")
    private String phone;

    /**
     * 物流托管费率
     */
    @JSONField(name = "logisitcs_escrow_fee_rate")
    private String logisitcsEscrowFeeRate;

    /**
     * 订单id
     */
    @JSONField(name = "order_id")
    private String orderId;

    /**
     *卖家全名
     */
    @JSONField(name = "seller_signer_fullname")
    private String sellerSignerFullname;


    /**
     *支付金額
     */
    @JSONField(name = "pay_amount")
    private AmountInfo payAmount;


    /**
     * 产品明细
     */
    private AliExpressOrderDetail detail;


    /**
     * 转单据状态
     * @author yl
     * @date 2023-11-29 16:10
     */
    public String convertBillStatus() {
        if (null == this.orderStatus || StringUtils.isBlank(this.orderStatus)){
            // 配货中
            return SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode();
        }
        // 已发货
        if ("WAIT_BUYER_ACCEPT_GOODS".equalsIgnoreCase(this.orderStatus)){
            return SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        }
        // 待发货
        return SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
    }
    /**
     * 转付款状态
     * @author yl
     * @date 2023-11-29 16:10
     */
    public String convertPayStatus() {
        String code = this.fundStatus;
        if(StringUtils.isBlank(code)){
            return SoB2cPayStatusEnum.ENUM_PAYMENT.getCode();
        }
        if("NOT_PAY".equals(code) ){
            return SoB2cPayStatusEnum.ENUM_PAYMENT.getCode();
        }
        if("PAY_SUCCESS".equals(code)||"WAIT_SELLER_CHECK".equals(code)){
            return SoB2cPayStatusEnum.ENUM_PAID.getCode();
        }
        return "unknow";
    }


}
