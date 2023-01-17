package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0
 * @description: API字段取值方式枚举
 * @date 2023/1/11 14:58
 */
public enum ErpPlatformSignEnum {

    MA_BANG_PLATFORM(1, "马帮"),
    GYY_PLATFORM(2, "管易"),
    KIND_DEE_PLATFORM(3, "金蝶云星空" ),
    ;


    private Integer code;

    private String name;

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    ErpPlatformSignEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ErpPlatformSignEnum getByCode(Integer code) {
        ErpPlatformSignEnum[] values = values();
        for (ErpPlatformSignEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static ErpPlatformSignEnum getByName(String name) {
        ErpPlatformSignEnum[] values = values();
        for (ErpPlatformSignEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
