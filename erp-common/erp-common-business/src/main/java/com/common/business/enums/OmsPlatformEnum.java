package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 中转仓平台枚举类
 *
 * @Author Cloud
 * @Date 2023/4/6 14:49
 **/
@Getter
public enum OmsPlatformEnum {
    OMS_GOOD_CANG("goodcang","谷仓海外仓"),
    OMS_IML("iml","艾姆勒海外仓"),
    OMS_ANTU("antu","安兔"),
    ;

    @EnumValue
    private final String code;
    private final String name;

    OmsPlatformEnum(String code, String name) {
        this.code = code;
        this.name = name;
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

    public static boolean isThirdWarehouse(String code) {
        OmsPlatformEnum[] values = values();
        for (OmsPlatformEnum value : values) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
