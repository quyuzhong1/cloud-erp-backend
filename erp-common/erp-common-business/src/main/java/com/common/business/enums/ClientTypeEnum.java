package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 客户端类型枚举
 * @author jack
 * @Date 2025-09-15
 */
public enum ClientTypeEnum implements EnumMessage {
    WEB("web", "web端"),
    APP("app", "app端");
    ;

    @EnumValue
    @JsonValue
    private String code;

    private String name;

    ClientTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
