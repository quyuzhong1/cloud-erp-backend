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
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_order_supplier")
public class PurchaseOrderSupplierEntity extends BaseEntity<PurchaseOrderSupplierEntity> {

    /**
     * 采购订单id
     */
    @TableField("purchase_order_id")
    private String purchaseOrderId;

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
    @TableField("settle_method")
    private String settleMethod;

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
    @TableField("contact_tel_number")
    private String contactTelNumber;


    public static final String PURCHASE_ORDER_ID = "purchase_order_id";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String SETTLE_METHOD = "settle_method";

    public static final String SETTLE_CURRENCY = "settle_currency";

    public static final String SUPPLIER_CONTACT_ID = "supplier_contact_id";

    public static final String CONTACT_NAME = "contact_name";

    public static final String CONTACT_TEL_NUMBER = "contact_tel_number";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
