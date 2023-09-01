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
    SUPPLIER_ACCOUNT_PAYMENT("supplierAccountPayment","供应商账户付款方式"),
    RECEIVE_METHOD("receiveMethod",  "收款方式"),
    COLLECTION_TERMS("collectionTerms",  "收款条件"),
    LOGISTICS_METHOD("logisticsMethod",  "b2c销售订单物流方式"),
    LOGIC("logic",  "订单规则的逻辑关系"),
    AND("and",  "且"),
    OR("or",  "或"),
    RULE_CONDITION("ruleCondition","审核规则"),

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

    public static String getName(String type) {
        for (DictBasicEnum dictBasic : DictBasicEnum.values()) {
            if (type.equals(dictBasic.getType())) {
                return dictBasic.getDesc();
            }
        }
        return "";
    }
}
