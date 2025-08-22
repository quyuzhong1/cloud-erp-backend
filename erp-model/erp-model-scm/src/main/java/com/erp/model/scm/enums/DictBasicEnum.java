package com.erp.model.scm.enums;

/**
 * @author Lambda
 * @Classname DictBasicEnum

 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictBasicEnum {

    SUPPLIER_PAY_MODE("supplierPayMode",  "供应商结算方式"),
    SUPPLIER_CATEGORY("supplierCategory" , "供应商分类"),
    SUPPLIER_ACCOUNT_PAYMENT("supplierAccountPayment","供应商账户付款方式"),
    SUBCONTRACT_CHANGE_REASON("subcontractChangeReason","委外变更原因"),
    PURCHASE_ORDER_TYPE("purchaseOrderType","采购订单单据类型"),
    CONTRACT_TYPE("contractType","采购合同管理"),
    CREDENTIAL_TYPE("credentialType","证照字典"),
    PROPERTY("property","供应商属性"),
    CERTIFICATE("certificate","体系认证"),

    ;


    private String type;
    private String desc;


    DictBasicEnum( String type, String desc) {

        this.type = type;
        this.desc = desc;
    }


    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
