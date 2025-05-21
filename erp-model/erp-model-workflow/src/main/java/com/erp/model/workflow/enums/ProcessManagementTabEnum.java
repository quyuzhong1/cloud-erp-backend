package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 流程管理tab枚举
 * @author will
 * @date 2025/5/12 18:17
 */
public enum ProcessManagementTabEnum {
    // 全部"
    ALL("all", "全部"),
    // 运行中
    RUNNING("running", "运行中"),
    // 已完成
    FINISH("finish", "已完成"),
    // 已结束
    ABNORMAL("abnormal", "异常流程"),
    ;
    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    ProcessManagementTabEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ProcessManagementTabEnum getByCode(String code) {
        return Arrays.stream(ProcessManagementTabEnum.values())
                .filter(state -> code.equals(state.getCode()))
                .findFirst().orElse(null);
    }

    public static String getName(String type) {
        for (ProcessManagementTabEnum statusEnum : ProcessManagementTabEnum.values()) {
            if (type.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
