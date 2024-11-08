package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 退款订单
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("refund_order")
public class RefundOrderEntity extends BaseEntity<RefundOrderEntity> {

    /**
     * 退款单号
     */
    @TableField("code")
    private String code;

    /**
     * 平台
     */
    @TableField("dict_platform")
    private String dictPlatform;

    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 平台订单号
     */
    @TableField("platform_order_no")
    private String platformOrderNo;

    /**
     * 平台退款单号
     */
    @TableField("platform_refund_no")
    private String platformRefundNo;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * 币别符号
     */
    @TableField("currency_symbol")
    private String currencySymbol;

    /**
     * 退款原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 销售订单id
     */
    @TableField("so_id")
    private String soId;

    /**
     * 销售订单编号
     */
    @TableField("so_code")
    private String soCode;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

    /**
     * 平台创建时间
     */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;

    /**
     * 退款金额人名币
     */
    @TableField("refund_cny_amount")
    private BigDecimal refundCnyAmount;


    public static final String CODE = "code";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String PLATFORM_ORDER_NO = "platform_order_no";

    public static final String PLATFORM_REFUND_NO = "platform_refund_no";

    public static final String STATUS = "status";

    public static final String REFUND_AMOUNT = "refund_amount";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String REASON = "reason";

    public static final String REFUND_TIME = "refund_time";

    public static final String REFUND_CNY_AMOUNT = "refund_cny_amount";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
