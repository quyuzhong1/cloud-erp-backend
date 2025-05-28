package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 第三方审核任务状态
 * @author will
 * @date 2025/5/27 10:24
 */
public enum ApproveTaskStatusEnum implements EnumMessage {
    ALL("all", "全部"),
    SUCCESS("success", "生成成功"),
    FAIL("fail", "生成失败"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    ApproveTaskStatusEnum(String code, String name) {
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
        for (ApproveTaskStatusEnum state : ApproveTaskStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static ApproveTaskStatusEnum getByCode(String code) {
        for (ApproveTaskStatusEnum state : ApproveTaskStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static List<ApproveTaskStatusEnum> getAll() {
        return Arrays.stream(ApproveTaskStatusEnum.values()).collect(Collectors.toList());
    }
}
