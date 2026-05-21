package com.erp.model.sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum MessageDispatchTaskStatusEnum {
    INIT("INIT", "待执行"),
    RUNNING("RUNNING", "执行中"),
    SUCCESS("SUCCESS", "执行成功"),
    FAILED("FAILED", "执行失败");

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    MessageDispatchTaskStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MessageDispatchTaskStatusEnum getByCode(String code) {
        for (MessageDispatchTaskStatusEnum item : values()) {
            if (Objects.equals(item.code, code)) {
                return item;
            }
        }
        return null;
    }
}
