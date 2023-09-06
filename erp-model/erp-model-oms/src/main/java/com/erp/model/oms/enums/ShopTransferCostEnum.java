package com.erp.model.oms.enums;

/**
 * @author Will
 * @version 1.0
 * @date 2023/9/6 18:03
 */
public enum ShopTransferCostEnum {
    MULTIPLY_TRANSFER_RATE("multiplyTransferRate",  "(订单销售金额+运费收入)*转账费率"),
    ;


    private String code;
    private String name;


    ShopTransferCostEnum(String code, String name) {

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
