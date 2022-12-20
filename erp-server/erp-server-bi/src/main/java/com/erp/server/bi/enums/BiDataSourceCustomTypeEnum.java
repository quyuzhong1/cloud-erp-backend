package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/20 9:12
 */
public enum BiDataSourceCustomTypeEnum {
    YEAR("1", "年"),
    MONTH("2", "季度"),
    QUARTER("3", "月"),
    WEEK("4", "周"),
    DAY("5", "日");

    private String code;
    private String name;

    BiDataSourceCustomTypeEnum(String code, String name) {
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
        for (BiDataSourceCustomTypeEnum biDataSourceCustomTypeEnum : BiDataSourceCustomTypeEnum.values()) {
            if (code.equals(biDataSourceCustomTypeEnum.getCode())) {
                return biDataSourceCustomTypeEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        BiDataSourceCustomTypeEnum[] enums = values();
        for (BiDataSourceCustomTypeEnum biDataSourceCustomTypeEnum : enums) {
            if (biDataSourceCustomTypeEnum.getName().equals(name)) {
                return biDataSourceCustomTypeEnum.getCode();
            }
        }
        return null;
    }
}
