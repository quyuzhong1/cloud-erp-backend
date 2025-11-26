package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliveryModeEnum implements EnumMessage {
    CHANNEL_ORDER("channelOrder","渠道订单"),
    TRUCK_ORDER("truckOrder","卡车订单"),
    SELF_EXTRACTION("selfExtraction","自提订单"),
    TRUCK_SELF_ORDER("truckSelfOrder","卡车自提"),
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




    DeliveryModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if(code == null){
            return "";
        }
        for (DeliveryModeEnum billTypeEnum : DeliveryModeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }
}
