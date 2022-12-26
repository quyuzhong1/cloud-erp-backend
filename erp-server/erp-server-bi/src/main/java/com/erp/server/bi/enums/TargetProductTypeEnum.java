package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/26 12:17
 */
public enum TargetProductTypeEnum {

    NEWPRODUCTS(0, "新品"),
    OLDPRODUCTS(1, "老品");

    private Integer code;
    private String name;

    TargetProductTypeEnum(Integer code, String name) {
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
        for (TargetProductTypeEnum typeEnum : TargetProductTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }

    public static Integer getCodeByName(String name) {
        TargetProductTypeEnum[] enums = values();
        for (TargetProductTypeEnum typeEnum : enums) {
            if (typeEnum.getName().equals(name)) {
                return typeEnum.getCode();
            }
        }
        return null;
    }
}
