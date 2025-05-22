package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 流程操作枚举
 * @author will
 * @date 2025/5/22 15:21
 */
public enum ProcessManagementOptionEnum {
    // 强制通过
    PASS("pass", "强制通过"),
    // 强制驳回
    REJECT("reject", "强制驳回"),
    // 恢复
    RESTORE("restore", "恢复"),
    // 暂停
    PAUSE("pause", "暂停"),
    ;
    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    ProcessManagementOptionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ProcessManagementOptionEnum getByCode(String code) {
        return Arrays.stream(ProcessManagementOptionEnum.values())
                .filter(state -> code.equals(state.getCode()))
                .findFirst().orElse(null);
    }
}
