package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author jack
 * @Classname BooleanStrEnum
 * @Date 2025-06-18
 */
public enum BooleanStrEnum implements EnumMessage {
    TRUE("true","是"),
    FALSE("false","否"),
    ;

    @EnumValue
    @JsonValue
    private String code;

    private String name;
    BooleanStrEnum(String code, String name) {
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
