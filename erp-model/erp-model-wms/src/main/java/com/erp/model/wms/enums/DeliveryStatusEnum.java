package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliveryStatusEnum {
    UN_SHIPPED("unShipped","未发货"),
    PARTIAL_SHIPMENT("partialShipment","部分发货"),
    COMPLETE_SHIPMENT("completeShipment","已发货"),
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


    DeliveryStatusEnum(String code, String name) {
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
        for (DeliveryStatusEnum deliveryStatusEnum : DeliveryStatusEnum.values()) {
            if (code.equals(deliveryStatusEnum.getCode())) {
                return deliveryStatusEnum.getName();
            }
        }
        return "";
    }
}
