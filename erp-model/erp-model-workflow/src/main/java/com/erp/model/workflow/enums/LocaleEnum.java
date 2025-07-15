package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抄送状态枚举
 * @author Cloud
 */

public enum LocaleEnum {
    LOCALE_ZH_CN("zh-CN", "中文"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    LocaleEnum(String code, String name) {
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
        for (LocaleEnum state : LocaleEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
