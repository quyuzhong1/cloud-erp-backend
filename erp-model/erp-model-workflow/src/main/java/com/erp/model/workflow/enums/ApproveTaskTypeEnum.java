package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 第三方审核任务类型
 * @author will
 * @date 2025/5/27 10:24
 */
public enum ApproveTaskTypeEnum implements EnumMessage {
    PUSH("push", "实例推送"),
    PULL("pull", "实例获取"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    ApproveTaskTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (ApproveTaskTypeEnum state : ApproveTaskTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static ApproveTaskTypeEnum getByCode(String code) {
        for (ApproveTaskTypeEnum state : ApproveTaskTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static List<ApproveTaskTypeEnum> getAll() {
        return Arrays.stream(ApproveTaskTypeEnum.values()).collect(Collectors.toList());
    }
}
