package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @Description 备案平台枚举
 */
public enum DeclarePlatformEnum implements EnumMessage {

    BAO_HONG("BaoHong", "保宏"),

    ;


    @JsonValue
    @EnumValue
    private String code;

    private String name;



    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    DeclarePlatformEnum(String code, String name){

        this.code = code;
        this.name = name;
    }

}
