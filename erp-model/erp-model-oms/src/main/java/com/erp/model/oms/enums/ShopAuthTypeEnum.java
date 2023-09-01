package com.erp.model.oms.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 店铺授权类型
 * @date 2023/9/1 11:08
 */
public enum ShopAuthTypeEnum {

    ENUM_ALL("all",  "全部"),
    ENUM_PART("part",  "部分"),
    ;


    private String code;
    private String name;


    ShopAuthTypeEnum(String code, String name) {

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
