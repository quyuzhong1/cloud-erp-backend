package com.erp.model.oms.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 店铺授权类型
 * @date 2023/9/1 11:08
 */
public enum AuthTypeEnum {

    MAIN("shopee_mian",  "虾皮主账户"),
    MERCHANT("shopee_merchant",  "商人"),
    SHOP("shopee_shop",  "商铺"),
    ;


    private String code;
    private String name;


    AuthTypeEnum(String code, String name) {

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
