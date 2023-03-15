package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_purchase_order_supplier")
public class ScmPurchaseOrderSupplierEntity extends BaseEntity<ScmPurchaseOrderSupplierEntity> {

    /**
     * 采购订单id
     */
    @TableField("purch_order_id")
    private String purchOrderId;

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
    @TableField("method")
    private String method;

    /**
     * 结算币种
     */
    @TableField("settle_currency")
    private String settleCurrency;

    /**
     * 供应商联系人表id
     */
    @TableField("supplier_contact_id")
    private String supplierContactId;

    /**
     * 联系人名称
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 联系人电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系人邮件
     */
    @TableField("contact_email")
    private String contactEmail;


    public static final String PURCH_ORDER_ID = "purch_order_id";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String METHOD = "method";

    public static final String SETTLE_CURRENCY = "settle_currency";

    public static final String SUPPLIER_CONTACT_ID = "supplier_contact_id";

    public static final String CONTACT_NAME = "contact_name";

    public static final String CONTACT_PHONE = "contact_phone";

    public static final String CONTACT_EMAIL = "contact_email";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
