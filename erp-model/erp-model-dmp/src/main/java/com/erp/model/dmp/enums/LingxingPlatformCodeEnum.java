package com.erp.model.dmp.enums;


import com.common.core.constant.EnumMessage;
import lombok.Getter;

@Getter
public enum LingxingPlatformCodeEnum implements EnumMessage {
    AMAZON("10001", "AMAZON"),
    SHOPIFY("10002", "Shopify"),
    EBAY("10003", "eBay"),
    WISH("10004", "Wish"),
    ALIEXPRESS("10005", "AliExpress"),
    SHOPEE("10006", "Shopee"),
    LAZADA("10007", "Lazada"),
    WALMART("10008", "Walmart"),
    CUSTOM("10009", "自定义平台"),
    WAYFAIR("10010", "Wayfair"),
    TIKTOK("10011", "TikTok"),
    MERCADO("10012", "MERCADO"),
    CDISCOUNT("10013", "CDISCOUNT"),
    NEWEGG("10014", "NEWEGG"),
    RAKUTEN("10015", "RAKUTEN"),
    SHOPLINE("10016", "SHOPLINE"),
    TEAPPLIX("10017", "TEAPPLIX"),
    SHOPLAZZA("10018", "SHOPLAZZA"),
    UEESHOP("10019", "UEESHOP"),
    COUPANG("10020", "COUPANG"),
    SHEIN("10021", "SHEIN"),
    TEMU_FBM("10022", "Temu全托管"),
    TEMU_FBP("10024", "Temu半托管"),
    OTTO("10025", "OTTO"),
    OZON("10026", "OZON"),
    SHEIN_FBM("10027", "SHEIN全托管"),
    SHEIN_FBP("10028", "SHEIN半托管"),
    ALIEXPRESS_FBP("10029", "AliExpress半托管"),
    ALIEXPRESS_FBM("10030", "AliExpress全托管"),
    QOO10("10033", "Qoo10"),
    MIRAKL("10034", "Mirakl"),
    LINE_SHOPPING("10038", "line shopping");

    private final String code;
    private final String name;

    LingxingPlatformCodeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LingxingPlatformCodeEnum fromCode(String code) {
        for (LingxingPlatformCodeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}