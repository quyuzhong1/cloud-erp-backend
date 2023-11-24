package com.common.business.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Lambda
 * @Classname OrderTypeEnum
 * @Description TODO
 * @Date 2023-11-23 19:18
 * @Created by yl
 */
public enum OrderTypeEnum implements EnumMessage {
    B2B("B2B", "B2B订单"),
    B2C("B2C", "B2C订单"),
    ;

    private String code;

    private String name;

    OrderTypeEnum(String code, String name) {
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
}
