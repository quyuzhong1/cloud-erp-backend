package com.erp.model.dmp.enums;

/**
 * 销售站点枚举类
 *
 * @Author Cloud
 * @Date 2022/12/19 11:01
 **/
public enum SalesSiteEnum {
    SHOPIFY(1, "Shopify", "Shopify"),


    ;


    private int code;

    private String name;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    SalesSiteEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }
    public static SalesSiteEnum getByCode(int code) {
        SalesSiteEnum[] values = values();
        for (SalesSiteEnum value : values) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
