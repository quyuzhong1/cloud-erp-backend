package com.erp.model.oms.enums;

/**
 * @author Lambda
 * @Classname DictBasicEnum

 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictBasicEnum {

    SUPPLIER_PAY_MODE("supplierPayMode",  "供应商结算方式"),
    SUPPLIER_CATEGORY("supplierCategory" , "供应商分类"),
    PLATFORM("platform" , "平台类型"),
    SALES_PLATFORM("salesPlatform" , "销售平台"),
    SUPPLIER_ACCOUNT_PAYMENT("supplierAccountPayment","供应商账户付款方式"),
    RECEIVE_METHOD("receiveMethod",  "收款方式"),
    COLLECTION_TERMS("collectionTerms",  "收款条件"),

    DELIVERY_MODE("deliveryMode",  "交货方式"),

    TRADE_TERM("tradeTerm",  "贸易条款"),


    ;


    private String type;
    private String desc;


    DictBasicEnum(String type, String desc) {

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
