package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 配货方式
 */
public enum JituDeliveryTypeEnum implements EnumMessage {
    PLATFORM_LOGISTICS("PTWL","平台物流"),
    SELF_PICK_UP("ZTJ","自提件"),
    SHOP_SELF_DELIVERY("SJZL","商家自联快递"),
    WAREHOUSE_DELIVERY("CPKD","仓配快递"),
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


    JituDeliveryTypeEnum(String code, String name) {
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
        for (JituDeliveryTypeEnum deliveryTypeEnum : JituDeliveryTypeEnum.values()) {
            if (code.equals(deliveryTypeEnum.getCode())) {
                return deliveryTypeEnum.getName();
            }
        }
        return "";
    }

    public static JituDeliveryTypeEnum getEnum(String code) {
        for (JituDeliveryTypeEnum deliveryTypeEnum : JituDeliveryTypeEnum.values()) {
            if (code.equals(deliveryTypeEnum.getCode())) {
                return deliveryTypeEnum;
            }
        }
        return null;
    }
}
