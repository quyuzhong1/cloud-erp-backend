package com.erp.model.sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum MessageDispatchTaskSceneEnum {
    PDA_NOTICE("PDA_NOTICE", "PDA系统公告分发"),
    PDA_UPGRADE_PUSH("PDA_UPGRADE_PUSH", "PDA升级通知实时推送");

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    MessageDispatchTaskSceneEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MessageDispatchTaskSceneEnum getByCode(String code) {
        for (MessageDispatchTaskSceneEnum item : values()) {
            if (Objects.equals(item.code, code)) {
                return item;
            }
        }
        return null;
    }
}
