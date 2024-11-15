package com.erp.server.bi.enums;

import java.util.Objects;

/**
 * 指标条件结算类型枚举
 *
 * @Author Cloud
 * @Date 2022/12/13 14:36
 **/
public enum SettleMethodEnum {
    // CNY实时
    CNY_CURRENT(0, "CNY实时", "使用实时汇率", "currency_rate"),
    // CNY结算
    CNY_SETTLE(1, "CNY结算", "使用结算汇率", "cny_settle_rate"),
    // 原币种
    ORIGINAL_CURRENCY(2, "原币种", "不适用汇率", "");


    private Integer code;

    private String name;

    private String desc;

    private String field;

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getField() {
        return field;
    }

    SettleMethodEnum(int code, String name, String desc, String field) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.field = field;
    }

    public static SettleMethodEnum getByCode(Integer code) {
        if (Objects.isNull(code)) {
            return SettleMethodEnum.ORIGINAL_CURRENCY;
        }
        SettleMethodEnum[] values = values();
        for (SettleMethodEnum value : values) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
