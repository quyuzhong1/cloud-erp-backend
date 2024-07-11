package com.erp.model.dmp.enums;

import java.util.Arrays;

/**
 *
 *
 * @Author Cloud
 * @Date 2023/3/27 17:37
 **/
public enum MabangSourcePlatformEnum {
    AMAZON_FBA("2", "Amazon", "Amazon"),
    ALIEXPRESS("3", "Aliexpress", "AliExpress"),
    CDISCOUNT_FBC("8", "Cdiscount FBC", ""),
    SHOPIFY("16", "Shopify", "Shopify"),
    SHOPEE("17", "Shopee", "Shopee"),
    ALIBABA_1688("18", "1688", "Alibaba"),
    WALMART("32", "Walmart", "Walmart"),
    TAOBAO("46", "淘宝", "TaoBao"),
    PINDUODUO("52", "拼多多", "PDD"),
    YOUZANYUN("55", "有赞云", "YouZan"),
    JD("65", "京东", "JD"),
    TIANMAO("69", "天猫", "Tmall");

    private final String code;
    private final String desc;
    private final String erpPlatformCode;

    MabangSourcePlatformEnum(String code, String desc, String erpPlatformCode) {
        this.code = code;
        this.desc = desc;
        this.erpPlatformCode = erpPlatformCode;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getErpPlatformCode() {
        return erpPlatformCode;
    }

    public static MabangSourcePlatformEnum getByCode(String code) {
        return Arrays.stream(MabangSourcePlatformEnum.values())
                .filter(platform -> platform.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static MabangSourcePlatformEnum getByDesc(String desc) {
        return Arrays.stream(MabangSourcePlatformEnum.values())
                .filter(platform -> platform.getDesc().equals(desc))
                .findFirst()
                .orElse(null);
    }
}
