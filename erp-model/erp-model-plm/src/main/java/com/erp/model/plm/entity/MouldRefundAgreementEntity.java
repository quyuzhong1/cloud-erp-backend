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
 * 合同返还约定
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mould_refund_agreement")
public class MouldRefundAgreementEntity extends BaseEntity<MouldRefundAgreementEntity> {

    /**
    * 模具id
    */
    @TableField("mould_detail_id")
    private String mouldDetailId;
    /**
    * 是否费用返还
    */
    @TableField("is_need_refund")
    private Boolean isNeedRefund;
    /**
    * 返还标准
    */
    @TableField("refund_standard")
    private String refundStandard;
    /**
    * 退款单量
    */
    @TableField("refund_order_qty")
    private Integer refundOrderQty;
    /**
    * 返还金额
    */
    @TableField("refund_amount")
    private BigDecimal refundAmount;
    /**
    * 费用返还状态
    */
    @TableField("refund_status")
    private String refundStatus;


    public static final String MOULD_DETAIL_ID = "mould_detail_id";

    public static final String IS_NEED_REFUND = "is_need_refund";

    public static final String REFUND_STANDARD = "refund_standard";

    public static final String REFUND_ORDER_QTY = "refund_order_qty";

    public static final String REFUND_AMOUNT = "refund_amount";

    public static final String REFUND_STATUS = "refund_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}