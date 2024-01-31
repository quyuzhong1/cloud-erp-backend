package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum QueryDataTypeEnum implements EnumMessage {

    STRING("string","字符串"),
    NUMBER("number","数字"),
    DATE("date","日期"),
    BOOLEAN("boolean","布尔值"),
    ;

    private final String code;

    private final String name;

    QueryDataTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
