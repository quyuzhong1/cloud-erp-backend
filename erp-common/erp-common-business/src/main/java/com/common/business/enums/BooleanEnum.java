package com.common.business.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Lambda
 * @Classname BoolEnum
 * @Description TODO
 * @Date 2023-10-07 16:55
 * @Created by yl
 */
public enum BooleanEnum implements EnumMessage {
    TRUE(Boolean.TRUE,"是"),
    FALSE(Boolean.FALSE,"否"),
    ;

    private Boolean code;

    private String name;
    BooleanEnum(Boolean code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Boolean getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
