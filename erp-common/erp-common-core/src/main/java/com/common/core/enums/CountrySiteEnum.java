package com.common.core.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * @Classname CurrencyEnum

 * @Date 2022-08-09 10:31
 * @Created by yl
 */
@Getter
public enum CountrySiteEnum {

    CHINA("China", "China", "CN","中国"),
    AMERICA("America", "America","US","美国"),
    CANADA("Canada", "Canada","CA","加拿大"),
    BRAZIL("Brazil", "002","BR","巴西"),
    MEXICO("Mexico", "018","MX","墨西哥"),
    ENGLAND("England", "007","UK","英国"),
    GERMANY("Germany", "021","DE","德国"),
    FRANCE("France", "015","FR","法国"),
    SPAIN("Spain", "032","ES","西班牙"),
    ITALY("Italy", "","IT","意大利"),
    NETHERLANDS("Netherlands", "025","NL","荷兰"),
    JAPAN("Japan", "003","JP","日本"),
    INDIA("INDIA", "014","IN","印度"),
    AUSTRALIA("Australia", "32","AU","澳大利亚"),
    ;


    private final String currencyCode;
    private final String kingDeeCode;
    private final String site;
    private final String currencyName;


    CountrySiteEnum(String currencyCode, String kingDeeCode, String site, String currencyName) {
        this.currencyCode = currencyCode;
        this.kingDeeCode = kingDeeCode;
        this.site = site;
        this.currencyName = currencyName;

    }

    public static CountrySiteEnum getByCode(String currencyCode) {
        CountrySiteEnum[] values = values();
        for (CountrySiteEnum value : values) {
            if (value.currencyCode.equals(currencyCode)) {
                return value;
            }
        }
        return null;
    }
    public static CountrySiteEnum getByKingDeeCode(String kingDeeCode) {
        return Arrays.stream(values()).filter(value -> value.getKingDeeCode().equals(kingDeeCode))
                .findFirst().orElse(null);
    }
}
