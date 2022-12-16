package com.erp.server.bi.enums;

/**
 * 指标条件时间类型枚举
 *
 * @Author Cloud
 * @Date 2022/12/13 14:36
 **/
public enum SettleMethodEnum {
    // CNY实时
    CNY_CURRENT(0,"CNY实时"),
    // CNY结算
    CNY_SETTLE( 1, "CNY结算"),
    // 原币种
    ORIGINAL_CURRENCY( 2, "原币种");

    private int code;

    private String value;


    SettleMethodEnum(int code, String value) {
        this.code = code;
        this.value= value;
    }

    public int getCode() {
        return code;
    }
    public String getValue() {
        return value;
    }
}
