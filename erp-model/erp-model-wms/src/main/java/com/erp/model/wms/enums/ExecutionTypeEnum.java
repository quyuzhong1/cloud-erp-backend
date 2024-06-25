package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExecutionTypeEnum implements EnumMessage {

    MANUAL("manual","手动执行"),
    AUTO("auto","自动执行"),

    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;


    ExecutionTypeEnum(String code, String name) {
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

    public static String getName(String code) {
        for (ExecutionTypeEnum settingEnum : ExecutionTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum.getName();
            }
        }
        return "";
    }

    public static ExecutionTypeEnum getEnum(String code) {
        for (ExecutionTypeEnum settingEnum : ExecutionTypeEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
