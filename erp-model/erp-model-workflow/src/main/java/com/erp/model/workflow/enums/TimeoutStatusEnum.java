package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抄送状态枚举
 * @author Cloud
 */

public enum TimeoutStatusEnum {
    UNSEND("0", "未发送"),
    SEND_WARN("1", "警告已发送"),
    SEND_HANDLE("2", "异常操作已发送"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    TimeoutStatusEnum(String code, String name) {
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
        for (TimeoutStatusEnum state : TimeoutStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static TimeoutStatusEnum getByCode(String code) {
        for (TimeoutStatusEnum state : TimeoutStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static List<TimeoutStatusEnum> getAll() {
        return Arrays.stream(TimeoutStatusEnum.values()).collect(Collectors.toList());
    }
}
