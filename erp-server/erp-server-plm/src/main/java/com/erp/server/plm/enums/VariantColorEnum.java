package com.erp.server.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/22 11:15
 */
public enum VariantColorEnum {

    COLOR_RED("R", "红"),
    COLOR_YELLOW("Y", "黄"),
    COLOR_BLUE("B","蓝");

    private String code;
    private String name;

    VariantColorEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (VariantColorEnum value : VariantColorEnum.values()) {
            if (code.equals(value.getCode())) {
                return value.getName();
            }
        }
        return "";
    }

    public static String getCode(String name) {
        for (VariantColorEnum value : VariantColorEnum.values()) {
            if (name.equals(value.getName())) {
                return value.getCode();
            }
        }
        return "";
    }
}
