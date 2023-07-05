package com.erp.model.dmp.enums;

/**
 * FBA发货单状态
 */
public enum FbaDeliveryStatusEnum {
    WAIT_DELIVERY(1, "待配货"),
    WAIT_SEND(2, "待发货"),
    SENDED(3, "已发货"),
    INVALID(4, "作废"),

    ;


    private int code;

    private String name;


    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    FbaDeliveryStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
    public static FbaDeliveryStatusEnum getByCode(int code) {
        FbaDeliveryStatusEnum[] values = values();
        for (FbaDeliveryStatusEnum value : values) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
