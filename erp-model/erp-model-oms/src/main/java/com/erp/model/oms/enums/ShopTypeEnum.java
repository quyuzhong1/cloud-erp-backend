package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 店铺类型
 */
public enum  ShopTypeEnum implements EnumMessage {

    CROSS_BORDER("crossBorder",  "跨境"),
    LOCAL("local",  "本地");

    @EnumValue
    @JsonValue
    private String code;
    private String name;


    ShopTypeEnum(String code, String name) {
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
}
