package com.erp.model.sys.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 三方通知推送失败类型枚举
 * @author jack
 * @date 2025-06-10
 */
public enum ThirdNoticePushFailedTypeEnum implements EnumMessage {
    NOPERSON("noPerson", "人员不存在失败"),
    SENDNOTICE("sendNoticeFailed", "发送消息失败"),
    ALL("all", "消费失败"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    ThirdNoticePushFailedTypeEnum(String code, String name) {
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
        for (ThirdNoticePushFailedTypeEnum state : ThirdNoticePushFailedTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static ThirdNoticePushFailedTypeEnum getByCode(String code) {
        for (ThirdNoticePushFailedTypeEnum state : ThirdNoticePushFailedTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static List<ThirdNoticePushFailedTypeEnum> getAll() {
        return Arrays.stream(ThirdNoticePushFailedTypeEnum.values()).collect(Collectors.toList());
    }
}
