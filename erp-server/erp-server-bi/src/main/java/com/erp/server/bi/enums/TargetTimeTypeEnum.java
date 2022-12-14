package com.erp.server.bi.enums;

/**
 * 指标条件时间类型枚举
 *
 * @Author Cloud
 * @Date 2022/12/13 14:36
 **/
public enum TargetTimeTypeEnum {
    // 订单时间类型
    ORDER_TIME(0,"订单时间"),
    // 发货时间类型
    DELIVERY_TIME( 1, "发货时间");

    private int code;

    private String value;


    TargetTimeTypeEnum(int code, String value) {
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
