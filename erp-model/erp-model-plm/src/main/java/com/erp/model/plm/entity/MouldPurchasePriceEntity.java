package com.erp.model.plm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 模具价目表
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mould_purchase_price")
public class MouldPurchasePriceEntity extends BaseEntity<MouldPurchasePriceEntity> {

    /**
    * 模具id
    */
    @TableField("mould_detail_id")
    private String mouldDetailId;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 含税单价
    */
    @TableField("tax_price")
    private BigDecimal taxPrice;
    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 结算方式
    */
    @TableField("pay_method_id")
    private String payMethodId;
    /**
    * 付款条件
    */
    @TableField("payment_condition")
    private String paymentCondition;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;


    public static final String MOULD_DETAIL_ID = "mould_detail_id";

    public static final String QTY = "qty";

    public static final String TAX_PRICE = "tax_price";

    public static final String TAX_RATE = "tax_rate";

    public static final String PAY_METHOD_ID = "pay_method_id";

    public static final String PAYMENT_CONDITION = "payment_condition";

    public static final String CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    @Override
    public Serializable pkVal() {
        return null;
    }

}