package com.common.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 月份枚举
 *
 * @Author Cloud
 * @Date 2022/12/22 14:48
 **/
public enum MonthEnum {
    january(1, "january"),
    february(2, "february"),
    march(3, "march"),
    april(4, "april"),
    may(5, "may"),
    june(6, "june"),
    july(7, "july"),
    august(8, "august"),
    september(9, "september"),
    october(10, "october"),
    november(11, "november"),
    december(12, "december"),
    ;

    private int code;
    private String msg;

    MonthEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;

    }

    public static List<String> getMonthList(int startMonth, int endMonth) {
        List<String> monthList = Arrays.stream(MonthEnum.values())
                .filter(x -> x.code >= startMonth && x.code <= endMonth)
                .map(MonthEnum::getMsg)
                .collect(Collectors.toList());
        return monthList;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
