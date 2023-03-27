package com.erp.model.dmp.enums;

import java.util.Arrays;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/3/27 17:37
 **/
public enum MabangSourcePlatformEnum {
    AMAZON_FBA("2", "Amazon FBA"),
    ALIEXPRESS("3", "Aliexpress"),
    CDISCOUNT_FBC("8", "Cdiscount FBC"),
    SHOPIFY("16", "Shopify"),
    SHOPEE("17", "Shopee"),
    ALIBABA_1688("18", "1688"),
    WALMART("32", "Walmart"),
    TAOBAO("46", "淘宝"),
    PINDUODUO("52", "拼多多"),
    YOUZANYUN("55", "有赞云"),
    JD("65", "京东"),
    TIANMAO("69", "天猫");

    private final String code;
    private final String desc;

    MabangSourcePlatformEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MabangSourcePlatformEnum getByCode(String code) {
        return Arrays.stream(MabangSourcePlatformEnum.values())
                .filter(platform -> platform.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
