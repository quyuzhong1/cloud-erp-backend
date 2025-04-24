package com.erp.server.bi.enums;

/**
 * 指标条件时间类型枚举
 *
 * @Author Cloud
 * @Date 2022/12/13 14:36
 **/
public enum TimeTypeEnum {
    // 订单时间类型
    ORDER_TIME(0,"订单时间","以订单创建时间为统计维度"),
    // 发货时间类型
    DELIVERY_TIME( 1, "发货时间","以订单发货时间为统计维度-财务");

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

    TimeTypeEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }
    public static TimeTypeEnum getByCode(int code) {
        TimeTypeEnum[] values = values();
        for (TimeTypeEnum value : values) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
