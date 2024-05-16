package com.erp.model.oms.enums;

/**
 * 店铺类型
 */
public enum  ShopTypeEnum {

    CROSS_BORDER("crossBorder",  "跨境"),
    LOCAL("local",  "本地");

    private String code;
    private String name;


    ShopTypeEnum(String code, String name) {
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
