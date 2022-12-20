package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 11:38
 */
public enum BiDataSourceCustomEnum {

    YEAR("year", "年份"),
    TARGETTYPE("targetType", "指标分类"),
    TARGETNAME("targetName", "指标名称"),
    TARGEVALUE("targetValue", "目标值");
    private String code;
    private String name;

    BiDataSourceCustomEnum(String code, String name) {
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
        for (BiDataSourceCustomEnum biDataSourceCustomEnum : BiDataSourceCustomEnum.values()) {
            if (code.equals(biDataSourceCustomEnum.getCode())) {
                return biDataSourceCustomEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        BiDataSourceCustomEnum[] enums = values();
        for (BiDataSourceCustomEnum biDataSourceCustomEnum : enums) {
            if (biDataSourceCustomEnum.getName().equals(name)) {
                return biDataSourceCustomEnum.getCode();
            }
        }
        return null;
    }
}
