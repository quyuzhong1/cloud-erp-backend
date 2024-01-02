package com.sdk.oms.shopee.enums;

public enum OrderStatusEnum {

    UNPAID("UNPAID",  "未付款"),
    READY_TO_SHIP("READY_TO_SHIP",  "准备发货"),
    PROCESSED("PROCESSED",  "已处理"),
    RETRY_SHIP("RETRY_SHIP",  "重新发货"),
    SHIPPED("SHIPPED",  "已发货"),
    TO_CONFIRM_RECEIVE("TO_CONFIRM_RECEIVE",  "确认签收"),
    COMPLETED("COMPLETED",  "已完成"),
    IN_CANCEL("IN_CANCEL",  "取消中"),
    CANCELLED("CANCELLED",  "已取消"),
    INVOICE_PENDING("INVOICE_PENDING",  "已开票"),
    TO_RETURN("TO_RETURN",  "去退货"),
    ;


    private String code;
    private String name;


    OrderStatusEnum(String code, String name) {

        this.code = code;
        this.name = name;
    }


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
