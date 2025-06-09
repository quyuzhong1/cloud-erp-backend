package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum ThirdWaresouseRequestStatusEnums implements EnumMessage {
    PROCESSING(0, "请求中"),
    SUCCESS(1, "请求成功"),
    FAILED(2, "请求失败");

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private Integer code;
    /**
     * 名称
     */
    private String name;

    ThirdWaresouseRequestStatusEnums(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(Integer code) {
        if (Objects.isNull(code)) {
            return "";
        }
        for (ThirdWaresouseRequestStatusEnums typeEnums : ThirdWaresouseRequestStatusEnums.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
