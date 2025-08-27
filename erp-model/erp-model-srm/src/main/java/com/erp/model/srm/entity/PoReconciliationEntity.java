package com.erp.model.srm.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * <p>
 * 采购对账单
 * </p>
 *
 * @author will
 * @since 2024-01-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("po_reconciliation")
public class PoReconciliationEntity extends BaseEntity<PoReconciliationEntity> {

    /**
    * 对账单号
    */
    @TableField("code")
    private String code;
    /**
    * 对账状态
    */
    @TableField("status")
    private String status;
    /**
    * 对账开始日期
    */
    @TableField("start_date")
    private LocalDate startDate;
    /**
    * 对账结束日期
    */
    @TableField("end_date")
    private LocalDate endDate;
    /**
    * 结算组织id
    */
    @TableField("settle_org_id")
    private String settleOrgId;
    /**
    * 结算组织名称
    */
    @TableField("settle_org_Name")
    private String settleOrgName;
    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 对账金额
    */
    @TableField("amount")
    private BigDecimal amount;
    /**
    * 币别
    */
    @TableField("currency")
    private String currency;
    /**
    * 生成对账日期
    */
    @TableField("reconciliation_date")
    private LocalDate reconciliationDate;
    /**
    * 供方确认日期
    */
    @TableField(value = "supplier_confirm_date",updateStrategy = FieldStrategy.IGNORED)
    private LocalDate supplierConfirmDate;
    /**
    * 采方确认日期
    */
    @TableField(value = "purchase_confirm_date",updateStrategy = FieldStrategy.IGNORED)
    private LocalDate purchaseConfirmDate;
    /**
    * 收到单据日期
    */
    @TableField("receive_date")
    private LocalDate receiveDate;
    /**
    * 供方确认人id
    */
    @TableField("supplier_confirm_user_id")
    private String supplierConfirmUserId;
    /**
    * 供方确认人名称
    */
    @TableField("supplier_confirm_user_name")
    private String supplierConfirmUserName;
    /**
    * 采方确认人id
    */
    @TableField("purchase_confirm_user_id")
    private String purchaseConfirmUserId;
    /**
    * 采方确认人名称
    */
    @TableField("purchase_confirm_user_name")
    private String purchaseConfirmUserName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;


    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String SETTLE_ORG_ID = "settle_org_id";

    public static final String SETTLE_ORG_NAME = "settle_org_Name";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String AMOUNT = "amount";

    public static final String CURRENCY = "currency";

    public static final String SUPPLIER_RECONCILIATION_USER_ID = "supplier_reconciliation_user_id";

    public static final String SUPPLIER_RECONCILIATION_USER_NAME = "supplier_reconciliation_user_name";

    public static final String PURCHASE_RECONCILIATION_USER_ID = "purchase_reconciliation_user_id";

    public static final String PURCHASE_RECONCILIATION_USER_NAME = "purchase_reconciliation_user_name";

    public static final String RECONCILIATION_DATE = "reconciliation_date";

    public static final String SUPPLIER_CONFIRM_DATE = "supplier_confirm_date";

    public static final String PURCHASE_CONFIRM_DATE = "purchase_confirm_date";

    public static final String RECEIVE_DATE = "receive_date";

    public static final String SUPPLIER_CONFIRM_USER_ID = "supplier_confirm_user_id";

    public static final String SUPPLIER_CONFIRM_USER_NAME = "supplier_confirm_user_name";

    public static final String PURCHASE_CONFIRM_USER_ID = "purchase_confirm_user_id";

    public static final String PURCHASE_CONFIRM_USER_NAME = "purchase_confirm_user_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}