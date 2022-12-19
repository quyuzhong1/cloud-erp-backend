package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 16:22
 */
public enum BiStateEnum {


    ENABLE(1, "启用"),
    DISABLE(0, "禁用");

    private Integer code;
    private String name;

    BiStateEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (BiStateEnum biStateEnum : BiStateEnum.values()) {
            if (code.equals(biStateEnum.getCode())) {
                return biStateEnum.getName();
            }
        }
        return "";
    }

    public static Integer getCodeByName(String name) {
        BiStateEnum[] enums = values();
        for (BiStateEnum biStateEnum : enums) {
            if (biStateEnum.getName().equals(name)) {
                return biStateEnum.getCode();
            }
        }
        return null;
    }
}
