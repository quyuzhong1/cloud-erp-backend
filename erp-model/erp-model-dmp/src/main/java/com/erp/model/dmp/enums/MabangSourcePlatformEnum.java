package com.erp.model.dmp.enums;

import java.util.Arrays;

/**
 *
 *
 * @Author Cloud
 * @Date 2023/3/27 17:37
 **/
public enum MabangSourcePlatformEnum {
    AMAZON_FBA("2", "Amazon", "亚马逊","Amazon"),
    ALIEXPRESS("3", "Aliexpress", "速卖通", "AliExpress"),
    CDISCOUNT_FBC("8", "Cdiscount FBC","Cdiscount", "Cdiscount"),
    SHOPIFY("16", "Shopify", "Shopify", "Shopify"),
    SHOPEE("17", "Shopee", "虾皮","Shopee"),
    ALIBABA_1688("18", "1688", "阿里巴巴","Alibaba"),
    WALMART("32", "Walmart", "沃尔玛","Walmart"),
    TAOBAO("46", "淘宝", "淘宝","TaoBao"),
    PINDUODUO("52", "拼多多", "淘宝","PDD"),
    YOUZANYUN("55", "有赞云", "有赞云","YouZan"),
    JD("65", "京东", "京东","JD"),
    TIANMAO("69", "天猫", "天猫","Tmall"),
    OTHER("80", "其他", "其他","Other"),
    TikTokShop("81", "TikTokShop", "TikTokShop","TikTok"),
    TikTok("82", "TikTok", "抖音小店","TikTok"),
    MERCADOLIBRE("83", "Mercadolibre", "美客多","mercadolibre"),
    ;

    private final String code;
    private final String desc;
    private final String name;
    private final String erpPlatformCode;

    MabangSourcePlatformEnum(String code, String desc, String name, String erpPlatformCode) {
        this.code = code;
        this.desc = desc;
        this.name = name;
        this.erpPlatformCode = erpPlatformCode;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return name;
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

    public static MabangSourcePlatformEnum getByName(String name) {
        return Arrays.stream(MabangSourcePlatformEnum.values())
                .filter(platform -> platform.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
