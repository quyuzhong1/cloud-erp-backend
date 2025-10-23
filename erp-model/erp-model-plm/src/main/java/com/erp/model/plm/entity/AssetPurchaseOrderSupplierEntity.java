package com.erp.model.plm.entity;

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
 * 
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_purchase_order_supplier")
public class AssetPurchaseOrderSupplierEntity extends BaseEntity<AssetPurchaseOrderSupplierEntity> {

    /**
    * 资产采购单id
    */
    @TableField("asset_purchase_order_id")
    private String assetPurchaseOrderId;
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
    * 结算方式
    */
    @TableField("pay_method_id")
    private String payMethodId;
    /**
     * 结算方式名称
     */
    @TableField("pay_method_name")
    private String payMethodName;
    /**
    * 结算币种
    */
    @TableField("pay_currency")
    private String payCurrency;
    /**
    * 供应商联系人id
    */
    @TableField("contact_id")
    private String contactId;
    /**
    * 供应商联系人名称
    */
    @TableField("contact_name")
    private String contactName;
    /**
    * 供应商电话
    */
    @TableField("contact_tel_number")
    private String contactTelNumber;
    /**
    * 付款条件
    */
    @TableField("payment_condition")
    private String paymentCondition;
    /**
    * 付款条件名称
    */
    @TableField("payment_condition_name")
    private String paymentConditionName;
    /**
    * 账户名称
    */
    @TableField("payee")
    private String payee;
    /**
    * 收款银行
    */
    @TableField("bank_name")
    private String bankName;
    /**
    * 银行账号
    */
    @TableField("bank_account")
    private String bankAccount;


    public static final String ASSET_PURCHASE_ORDER_ID = "asset_purchase_order_id";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String PAY_METHOD_ID = "pay_method_id";

    public static final String PAY_CURRENCY = "pay_currency";

    public static final String CONTACT_ID = "contact_id";

    public static final String CONTACT_NAME = "contact_name";

    public static final String CONTACT_TEL_NUMBER = "contact_tel_number";

    public static final String PAYMENT_CONDITION = "payment_condition";

    public static final String PAYMENT_CONDITION_NAME = "payment_condition_name";

    public static final String PAYEE = "payee";

    public static final String BANK_NAME = "bank_name";

    public static final String BANK_ACCOUNT = "bank_account";

    @Override
    public Serializable pkVal() {
        return null;
    }

}