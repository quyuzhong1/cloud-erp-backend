package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 委托审核状态枚举
 * @author will
 * @date 2025/5/12 18:17
 */
public enum ProcessDelegateStatusEnum {
    // 全部"
    ALL("all", "全部"),
    // 待执行
    PENDING("pending", "待执行"),
    // 运行中
    RUNNING("running", "运行中"),
    // 已结束
    ENDED("ended", "已结束"),
    ;
    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    ProcessDelegateStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ProcessDelegateStatusEnum getByCode(String code) {
        return Arrays.stream(ProcessDelegateStatusEnum.values())
                .filter(state -> code.equals(state.getCode()))
                .findFirst().orElse(null);
    }

    public static String getName(String type) {
        for (ProcessDelegateStatusEnum statusEnum : ProcessDelegateStatusEnum.values()) {
            if (type.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
