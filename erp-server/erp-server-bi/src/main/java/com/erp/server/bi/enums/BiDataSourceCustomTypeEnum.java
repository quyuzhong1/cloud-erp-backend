package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/20 9:12
 */
public enum BiDataSourceCustomTypeEnum {
    YEAR(1, "年","年趋势"),
    QUARTER(2, "季度","季度趋势"),
    MONTH(3, "月","月趋势"),
    WEEK(4, "周","周趋势"),
    DAY(5, "日","日趋势");

    private Integer code;
    private String name;
    private String desc;

    BiDataSourceCustomTypeEnum(Integer code, String name,String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public static String getName(Integer code) {
        for (BiDataSourceCustomTypeEnum biDataSourceCustomTypeEnum : BiDataSourceCustomTypeEnum.values()) {
            if (code.equals(biDataSourceCustomTypeEnum.getCode())) {
                return biDataSourceCustomTypeEnum.getName();
            }
        }
        return "";
    }

    public static Integer getCodeByName(String name) {
        BiDataSourceCustomTypeEnum[] enums = values();
        for (BiDataSourceCustomTypeEnum biDataSourceCustomTypeEnum : enums) {
            if (biDataSourceCustomTypeEnum.getName().equals(name)) {
                return biDataSourceCustomTypeEnum.getCode();
            }
        }
        return null;
    }
}
