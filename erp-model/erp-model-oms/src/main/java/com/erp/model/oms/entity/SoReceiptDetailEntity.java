package com.erp.model.oms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 收款单明细
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_receipt_detail")
public class SoReceiptDetailEntity extends BaseEntity<SoReceiptDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 销售id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售单号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 付款流水号
    */
    @TableField("payment_no")
    private String paymentNo;
    /**
    * 收款方式
    */
    @TableField("dict_receipt_method")
    private String dictReceiptMethod;
    /**
    * 收款账号
    */
    @TableField("receipt_account")
    private String receiptAccount;
    /**
    * 收款日期
    */
    @TableField("receipt_date")
    private LocalDate receiptDate;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 收款金额
    */
    @TableField("receipt_amount")
    private BigDecimal receiptAmount;


    public static final String MAIN_ID = "main_id";

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String PAYMENT_NO = "payment_no";

    public static final String DICT_RECEIPT_METHOD = "dict_receipt_method";

    public static final String RECEIPT_ACCOUNT = "receipt_account";

    public static final String RECEIPT_DATE = "receipt_date";

    public static final String REMARK = "remark";

    public static final String RECEIPT_AMOUNT = "receipt_amount";

    @Override
    public Serializable pkVal() {
        return null;
    }

}