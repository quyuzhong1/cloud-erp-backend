package com.erp.model.sys.enums;

/**
 * @ClassName DictBasicEnum
 * @Author: zhangchunlin
 * @Date: 2023/6/21 15:21
 * @Description: 字典
 */

public enum SysDictBasicEnum {

    PAYMENT_CONDITION("paymentCondition",  "付款条件"),
    SUPPLIER_CATEGORY("paymentCondition",  "付款条件"),
    ;


    private String code;
    private String name;


    SysDictBasicEnum(String code, String name) {

        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
