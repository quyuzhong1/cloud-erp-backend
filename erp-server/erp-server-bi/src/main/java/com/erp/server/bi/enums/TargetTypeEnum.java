package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/26 12:12
 */
public enum TargetTypeEnum {

    SALESQTY(0, "销量"),
    SALESVOLUME(1, "销售额");

    private Integer code;
    private String name;

    TargetTypeEnum(Integer code, String name) {
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
        for (TargetTypeEnum typeEnum : TargetTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }

    public static Integer getCodeByName(String name) {
        TargetTypeEnum[] enums = values();
        for (TargetTypeEnum typeEnum : enums) {
            if (typeEnum.getName().equals(name)) {
                return typeEnum.getCode();
            }
        }
        return null;
    }
}
