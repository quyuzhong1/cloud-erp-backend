package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 中转仓平台枚举类
 *
 * @Author Cloud
 * @Date 2023/4/6 14:49
 **/
public enum OmsPlatformEnum {

    OMS_GOOD_CANG("goodcang","谷仓"),

    OMS_IML("iml","艾姆勒")
    ;

    @EnumValue
    private String code;
    private String name;

    OmsPlatformEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode(){
        return this.code;
    }

    public String getName(){
        return this.name;
    }

    public static OmsPlatformEnum getByCode(String code) {
        OmsPlatformEnum[] values = values();
        for (OmsPlatformEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
