package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author jack
 * @Classname BooleanStrEnum
 * @Date 2025-06-18
 */
public enum ImportTypeEnum implements EnumMessage {
    ADD("add","新增"),
    UPDATE("update","更新"),
    ADD_OR_UPDATE("addOrUpdate","新增或更新"),
    DELETE("delete","删除"),
    ;

    @EnumValue
    @JsonValue
    private String code;

    private String name;
    ImportTypeEnum(String code, String name) {
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
