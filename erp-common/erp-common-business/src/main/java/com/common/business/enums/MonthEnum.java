package com.common.business.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/22 14:42
 */
public enum MonthEnum implements EnumMessage {

    JANUARY("1", "一月","一月"),
    FEBRUARY("2", "二月","二月"),
    MARCH("3", "三月","三月"),
    APRIL("4", "四月","四月"),
    MAY("5", "五月","五月"),
    JUNE("6", "六月","六月"),
    JULY("7", "七月","七月"),
    AUGUST("8", "八月","八月"),
    SEPTEMBER("9", "九月","九月"),
    OCTOBER("10", "十月","十月"),
    NOVEMBER("11", "十一月","十一月"),
    DECEMBER("12", "十二月","十二月");

    private String code;

    private String name;

    private String desc;

    MonthEnum(String code, String name,String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public static MonthEnum getNameByCode(String code) {
        MonthEnum[] enums = values();
        for (MonthEnum plmEnum : enums) {
            if (plmEnum.getCode().equals(code)) {
                return plmEnum;
            }
        }
        return null;
    }
}
