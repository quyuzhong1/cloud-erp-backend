package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 配送渠道
 */
public enum DeliveryChannelsEnum implements EnumMessage {
    SELF_DELIVERY("selfDelivery","卖家自配送"),
    AMAZON_DELIVERY("amazonDelivery","亚马逊配送"),
    ;

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


    DeliveryChannelsEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DeliveryChannelsEnum channelsEnum : DeliveryChannelsEnum.values()) {
            if (code.equals(channelsEnum.getCode())) {
                return channelsEnum.getName();
            }
        }
        return "";
    }

}
