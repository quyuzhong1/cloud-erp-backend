package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 销售平台枚举类
 *
 * @Author Cloud
 * @Date 2022/12/19 11:01
 **/
public enum SalesPlatformEnum implements EnumMessage {
    SHOPIFY("1", "Shopify", "Shopify", "004"),
    NASDAQ_JD("2", "京东", "京东", "007"),
    SHOPEE("3", "Shopee", "虾皮", "003"),
    WALMART("4", "Walmart", "沃尔玛", "018"),
    ALI_EXPRESS("5", "速卖通", "AliExpress", "002"),
    TIK_TOK_CN("6", "抖音/今日头条/鲁班", "抖音中国", "013"),
    TAO_BAO("7", "淘宝", "淘宝", "005"),
    ALIBABA("8", "阿里巴巴", "Alibaba", "008"),
    YOU_ZAN("9", "有赞微商城", "有赞微商城", "015"),
    AMAZON("10", "亚马逊", "亚马逊", "100"),
    OTHER_PLATFORM("11", "其他", "其他平台", "999"),
    B2B("12", "B2B", "B2B", "300"),
    LITTLE_RED_BOOK("13", "小红书", "RED", "014"),
    PDD("14", "拼多多", "Temu", "009"),
    TMALL("15", "天猫", "Tmall", "006"),
    SOP("16", "京东自营厂送", "京东自营", "007"),


    ;

    @JsonValue
    @EnumValue
    private String code;

    private String name;

    private String desc;

    private String kingdeeCode;

    public String getDesc() {
        return desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getKingdeeCode() {
        return kingdeeCode;
    }

    SalesPlatformEnum(String code, String name, String desc, String kingdeeCode) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.kingdeeCode = kingdeeCode;
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
