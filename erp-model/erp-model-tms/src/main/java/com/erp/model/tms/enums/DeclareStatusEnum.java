package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum DeclareStatusEnum implements EnumMessage {
    WAIT("wait","待确认"),
    //增加一个‘已确认’的枚举
    CONFIRMED("confirmed","已确认"),
    DECLARED("declared","已报关"),
//    INVALID("invalid","已作废"),
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


    DeclareStatusEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DeclareStatusEnum statusEnum : DeclareStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static DeclareStatusEnum getEnum(String code) {
        for (DeclareStatusEnum declareStatusEnum : DeclareStatusEnum.values()) {
            if (code.equals(declareStatusEnum.getCode())) {
                return declareStatusEnum;
            }
        }
        return null;
    }
}
