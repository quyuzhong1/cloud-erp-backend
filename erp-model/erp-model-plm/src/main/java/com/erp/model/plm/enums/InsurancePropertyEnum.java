package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jack
 * @Classname InsurancePropertyEnum
 * @Date 2025-03-07
 */
public enum InsurancePropertyEnum implements EnumMessage {
    NOT("not ", "无"),
    ELECTRIFIED("electrified ", "带电"),
    BATTERY("battery ", "带电池"),
    ;

    private String code;

    private String name;

    InsurancePropertyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (InsurancePropertyEnum item : InsurancePropertyEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
    public static String getCode(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        for (InsurancePropertyEnum item : InsurancePropertyEnum.values()) {
            if (name.equals(item.getName())) {
                return item.getCode();
            }
        }
        return "";
    }
}
