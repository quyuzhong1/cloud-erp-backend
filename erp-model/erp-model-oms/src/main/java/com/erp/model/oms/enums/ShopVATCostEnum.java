package com.erp.model.oms.enums;

/**
 * @author Will
 * @version 1.0
 * @date 2023/9/6 18:03
 */
public enum ShopVATCostEnum {
    MULTIPLY_VAT_RATE("multiplyVATRate",  "(订单销售金额+运费收入)*VAT费率"),
    MULTIPLY_ADD_VAT_RATE("multiplyAddVATRate",  "(订单销售金额+运费收入)*(1+VAT费率)*VAT费率"),
    DIVISION_ADD_MULTIPLY_VAT_RATE("divisionAddMultiplyVATRate",  "(订单销售金额+运费收入)/(1+VAT费率)*VAT费率"),
    ;


    private String code;
    private String name;


    ShopVATCostEnum(String code, String name) {

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
