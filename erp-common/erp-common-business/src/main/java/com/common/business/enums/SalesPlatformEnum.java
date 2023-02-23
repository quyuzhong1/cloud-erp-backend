package com.common.business.enums;

import com.common.core.constant.EnumMessage;

/**
 * 销售平台枚举类
 *
 * @Author Cloud
 * @Date 2022/12/19 11:01
 **/
public enum SalesPlatformEnum implements EnumMessage {
    SHOPIFY("1", "Shopify", "Shopify"),
    NASDAQ_JD("2", "京东", "京东"),
    SHOPEE("3", "Shopee", "虾皮"),
    WALMART("4", "Walmart", "沃尔玛"),
    ALI_EXPRESS("5", "速卖通", "AliExpress"),
    TIK_TOK_CN("6", "抖音/今日头条/鲁班", "抖音中国"),
    TAO_BAO("7", "淘宝", "淘宝"),
    ALIBABA("8", "阿里巴巴", "Alibaba"),
    YOU_ZAN("9", "有赞微商城", "有赞微商城"),
    AMAZON("10", "亚马逊", "亚马逊"),
    OTHER_PLATFORM("11", "其他", "其他平台"),
    B2B("12", "B2B", "B2B"),
    LITTLE_RED_BOOK("13", "小红书", "RED"),
    PDD("14", "拼多多", "Temu"),
    TMALL("15", "天猫", "Tmall"),
    SOP("16", "京东自营厂送", "京东自营"),


    ;

    private String code;

    private String name;

    private String desc;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    SalesPlatformEnum(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static SalesPlatformEnum getByCode(String code) {
        SalesPlatformEnum[] values = values();
        for (SalesPlatformEnum value : values) {
            if (value.code.equals(code) ) {
                return value;
            }
        }
        return null;
    }

    public static String getNameByName(String name) {
        SalesPlatformEnum[] values = values();
        for (SalesPlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value.getName();
            }
        }
        return "";
    }

    public static SalesPlatformEnum getByName(String name) {
        SalesPlatformEnum[] values = values();
        for (SalesPlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}
