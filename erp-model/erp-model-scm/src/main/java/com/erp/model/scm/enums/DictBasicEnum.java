package com.erp.model.scm.enums;

/**
 * @author Lambda
 * @Classname DictBasicEnum
 * @Description TODO
 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictBasicEnum {

    SUPPLIER_PAY_MODE("supplierPayMode",  "供应商结算方式"),
    SUPPLIER_CATEGORY("supplierCategory" , "供应商分类"),
    SUPPLIER_ACCOUNT_PAYMENT("supplierAccountPayment","供应商账户付款方式");


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
