package com.common.business.enums;

import com.common.core.constant.EnumMessage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2023/2/22 14:42
 */
public enum MonthEnum implements EnumMessage {

    JANUARY("1", "一月","january"),
    FEBRUARY("2", "二月","february"),
    MARCH("3", "三月","march"),
    APRIL("4", "四月","april"),
    MAY("5", "五月","may"),
    JUNE("6", "六月","june"),
    JULY("7", "七月","july"),
    AUGUST("8", "八月","august"),
    SEPTEMBER("9", "九月","september"),
    OCTOBER("10", "十月","october"),
    NOVEMBER("11", "十一月","november"),
    DECEMBER("12", "十二月","december");

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

    public static List<String> getMonthList(int startMonth, int endMonth) {
        return Arrays.stream(MonthEnum.values())
                .filter(x -> Integer.parseInt(x.code) >= startMonth &&Integer.parseInt(x.code) <= endMonth)
                .map(MonthEnum::getDesc)
                .collect(Collectors.toList());
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
