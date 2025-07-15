package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 流程来源平台枚举
 * @author will
 * @date 2025/5/23 14:23
 */
public enum ProcessSourcePlatformEnum {
    // 飞书
    FS("fs", "飞书审核"),
    // 数大臣
    ERP("erp", "数大臣审核"),
    ;
    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    ProcessSourcePlatformEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ProcessSourcePlatformEnum getByCode(String code) {
        return Arrays.stream(ProcessSourcePlatformEnum.values())
                .filter(state -> code.equals(state.getCode()))
                .findFirst().orElse(null);
    }

    public static String getName(String code) {
        for (ProcessSourcePlatformEnum statusEnum : ProcessSourcePlatformEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
