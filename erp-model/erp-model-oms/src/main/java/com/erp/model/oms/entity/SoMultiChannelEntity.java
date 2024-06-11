package com.erp.model.oms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 多渠道订单
 * </p>
 *
 * @author Jim
 * @since 2024-05-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_multi_channel")
public class SoMultiChannelEntity extends BaseEntity<SoMultiChannelEntity> {

    /**
    * 单据编码
    */
    @TableField("code")
    private String code;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 平台订单号
    */
    @TableField("platform_code")
    private String platformCode;
    /**
    * 销售平台
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 店铺
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * (手动)作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 作废原因
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
    * 订单状态
    */
    @TableField("bill_status")
    private String billStatus;
    /**
    * 付款状态（待付款、已付款）
    */
    @TableField("pay_status")
    private String payStatus;
    /**
    * 订单金额
    */
    @TableField("amount")
    private BigDecimal amount;
    /**
    * 币别（原币）
    */
    @TableField("currency")
    private String currency;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 运费收入
    */
    @TableField("shipping_fee")
    private BigDecimal shippingFee;
    /**
    * 付款时间
    */
    @TableField("pay_time")
    private LocalDateTime payTime;
    /**
    * 付款金额
    */
    @TableField("pay_amount")
    private BigDecimal payAmount;
    /**
    * 付款方式
    */
    @TableField("dict_pay_method")
    private String dictPayMethod;
    /**
    * 买家备注
    */
    @TableField("buyer_remark")
    private String buyerRemark;
    /**
    * 订单备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 销售组织id
    */
    @TableField("org_id")
    private String orgId;
    /**
    * 销售组织名称
    */
    @TableField("org_name")
    private String orgName;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源编码
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 标签json
    */
    @TableField("label_json")
    private String labelJson;
    /**
    * 订单日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
    /**
    * 作废类型（manual手动作废，automatic自动作废）
    */
    @TableField("invalid_type")
    private String invalidType;
    /**
    * 平台创建时间
    */
    @TableField("platform_order_create_time")
    private LocalDateTime platformOrderCreateTime;
    /**
    * 第三方仓发货订单id
    */
    @TableField("shipping_order_no")
    private String shippingOrderNo;
    /**
    * 是否冻结
    */
    @TableField("is_frozen")
    private Boolean isFrozen;
    /**
    * 扩展的 值 当后续有需要扩展的类型的字段值存里面
    */
    @TableField("extend_data")
    private String extendData;
    /**
    * 平台订单状态
    */
    @TableField("platform_order_status")
    private String platformOrderStatus;
    /**
    * 平台是否取消
    */
    @TableField("is_cancel")
    private Boolean isCancel;
    /**
    * 卖家订单编号
    */
    @TableField("seller_order_code")
    private String sellerOrderCode;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审核人姓名
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
     * 店铺名称
     */
    @TableField(exist = false)
    private String shopName;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String PLATFORM_CODE = "platform_code";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String SHOP_ID = "shop_id";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String BILL_STATUS = "bill_status";

    public static final String PAY_STATUS = "pay_status";

    public static final String AMOUNT = "amount";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String SHIPPING_FEE = "shipping_fee";

    public static final String PAY_TIME = "pay_time";

    public static final String PAY_AMOUNT = "pay_amount";

    public static final String DICT_PAY_METHOD = "dict_pay_method";

    public static final String BUYER_REMARK = "buyer_remark";

    public static final String REMARK = "remark";

    public static final String ORG_ID = "org_id";

    public static final String ORG_NAME = "org_name";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String LABEL_JSON = "label_json";

    public static final String BILL_DATE = "bill_date";

    public static final String INVALID_TYPE = "invalid_type";

    public static final String PLATFORM_ORDER_CREATE_TIME = "platform_order_create_time";

    public static final String SHIPPING_ORDER_NO = "shipping_order_no";

    public static final String IS_FROZEN = "is_frozen";

    public static final String EXTEND_DATA = "extend_data";

    public static final String PLATFORM_ORDER_STATUS = "platform_order_status";

    public static final String IS_CANCEL = "is_cancel";

    public static final String SELLER_ORDER_CODE = "seller_order_code";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}