package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 流程抄送选项枚举
 * @author will
 * @date 2025/5/20 15:53
 */
public enum ProcessCopyOptionEnum {
    // 角色
    ROLE("role", "角色"),
    // 指定人
    USER("somebody", "指定人"),
    ;
    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    ProcessCopyOptionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ProcessCopyOptionEnum getByCode(String code) {
        return Arrays.stream(ProcessCopyOptionEnum.values())
                .filter(state -> code.equals(state.getCode()))
                .findFirst().orElse(null);
    }
}
