package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname BoolEnum
 * @Description TODO
 * @Date 2023-10-07 16:55
 * @Created by yl
 */
public enum BooleanEnum implements EnumMessage {
    TRUE(true,"是"),
    FALSE(false,"否"),
    ;

    @EnumValue
    @JsonValue
    private Boolean code;

    private String name;
    BooleanEnum(Boolean code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Boolean getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getByCode(Boolean code) {
        for (BooleanEnum value : BooleanEnum.values()) {
            if (value.getCode().equals(code)) {
                return value.getName();
            }
        }
        return "";
    }

    public static Boolean getByName(String name) {
        for (BooleanEnum value : BooleanEnum.values()) {
            if (value.getName().equals(name)) {
                return value.getCode();
            }
        }
        return null;
    }
}
