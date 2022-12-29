package com.erp.server.bi.enums;

/**
 * 指标条件结算类型枚举
 *
 * @Author Cloud
 * @Date 2022/12/13 14:36
 **/
public enum SettleMethodEnum {
    // CNY实时
    CNY_CURRENT(0,"CNY实时","使用实时汇率"),
    // CNY结算
    CNY_SETTLE( 1, "CNY结算","使用结算汇率"),
    // 原币种
    ORIGINAL_CURRENCY( 2, "原币种", "不适用汇率");


    private int code;

    private String name;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    SettleMethodEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }
    public static SettleMethodEnum getByCode(int code) {
        SettleMethodEnum[] values = values();
        for (SettleMethodEnum value : values) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
