package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * erp审批同步失败类型枚举
 * @author jack
 * @date 2025-06-10
 */
public enum ApproveSyncFailedTypeEnum implements EnumMessage {
    CREATEINSTANCE("createInstanceFailed", "创建实例失败"),
    SENDNOTICE("sendNoticeFailed", "发送消息失败"),
    SENDAPPROVENOTICE("sendApproveNoticeFailed", "发送审批消息失败"),
    UPDATEAPPROVENOTICE("updateApproveNoticeFailed", "更新审批消息失败"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    ApproveSyncFailedTypeEnum(String code, String name) {
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
        for (ApproveSyncFailedTypeEnum state : ApproveSyncFailedTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static ApproveSyncFailedTypeEnum getByCode(String code) {
        for (ApproveSyncFailedTypeEnum state : ApproveSyncFailedTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static List<ApproveSyncFailedTypeEnum> getAll() {
        return Arrays.stream(ApproveSyncFailedTypeEnum.values()).collect(Collectors.toList());
    }
}
