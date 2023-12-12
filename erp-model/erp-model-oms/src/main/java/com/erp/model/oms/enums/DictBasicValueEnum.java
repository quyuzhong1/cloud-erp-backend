package com.erp.model.oms.enums;

/**
 * @author Lambda
 * @Classname DictBasicEnum

 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictBasicValueEnum {

    ONLINE_STORE_PAYMENT("onlineStorePayment",  "网店销售收款","collectionTerms"),
    ;


    private String code;
    private String name;
    private String type;

    DictBasicValueEnum(String code, String name,String type) {

        this.code = code;
        this.name = name;
        this.type = type;
    }


    public String getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (DictBasicValueEnum dictBasic : DictBasicValueEnum.values()) {
            if (code.equals(dictBasic.getCode())) {
                return dictBasic.getName();
            }
        }
        return "";
    }
}
