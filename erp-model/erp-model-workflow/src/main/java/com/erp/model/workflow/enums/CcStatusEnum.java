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

public enum CcStatusEnum {
    SEND("send", "已发送"),
    UNSEND("unsend", "未发送"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    CcStatusEnum(String code, String name) {
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
        for (CcStatusEnum state : CcStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static CcStatusEnum getByCode(String code) {
        for (CcStatusEnum state : CcStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static List<CcStatusEnum> getAll() {
        return Arrays.stream(CcStatusEnum.values()).collect(Collectors.toList());
    }
}
