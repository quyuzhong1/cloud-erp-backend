package com.erp.model.dmp.enums;

/**
 * @Author: wtr
 * @Date: 2026/3/11 17:43
 * @Param:
 * @Return:
 * @Description:
 **/

public enum  ThirdMappingSystemEnum {

    LINGXING("lingxing", "领星"),
    WDT("wdt", "旺店通"),
    ERP("erp", "自研ERP"),

    ;

    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    ThirdMappingSystemEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ThirdMappingSystemEnum getByCode(String code) {
        ThirdMappingSystemEnum[] values = values();
        for (ThirdMappingSystemEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
    public static String getNameByCode(String code) {
        ThirdMappingSystemEnum[] values = values();
        for (ThirdMappingSystemEnum value : values) {
            if (value.code.equals(code)) {
                return value.getName();
            }
        }
        return null;
    }

    public static ThirdMappingSystemEnum getByName(String name) {
        ThirdMappingSystemEnum[] values = values();
        for (ThirdMappingSystemEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}