package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 配货方式
 */
public enum DeliveryTypeEnum implements EnumMessage {
    DOOR_PICKUP("DOOR_PICKUP","上门揽收"),
    SELF_POST("SELF_POST","自寄"),
    SELF_SEND("SELF_SEND","自送"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;


    DeliveryTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    public static String getName(String code) {
        for (DeliveryTypeEnum deliveryTypeEnum : DeliveryTypeEnum.values()) {
            if (code.equals(deliveryTypeEnum.getCode())) {
                return deliveryTypeEnum.getName();
            }
        }
        return "";
    }

    public static DeliveryTypeEnum getEnum(String code) {
        for (DeliveryTypeEnum deliveryTypeEnum : DeliveryTypeEnum.values()) {
            if (code.equals(deliveryTypeEnum.getCode())) {
                return deliveryTypeEnum;
            }
        }
        return null;
    }
}
