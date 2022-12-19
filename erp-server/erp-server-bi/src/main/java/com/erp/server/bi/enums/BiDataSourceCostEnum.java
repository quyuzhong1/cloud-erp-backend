package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 11:38
 */
public enum BiDataSourceCostEnum {

    MONTH("month", "月份"),
    DEPTNAME("deptName","销售事业部"),
    PLATFORMNAME("platformName", "平台"),
    SITE("site", "站点"),
    SHOPNAME("shopName", "店铺名称"),
    CHARGENAME("chargeName", "负责人"),
    COMBINATION("combination", "组合");

    private String code;
    private String name;

    BiDataSourceCostEnum(String code, String name) {
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
        for (BiDataSourceCostEnum biDataSourceCostEnum : BiDataSourceCostEnum.values()) {
            if (code.equals(biDataSourceCostEnum.getCode())) {
                return biDataSourceCostEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        BiDataSourceCostEnum[] enums = values();
        for (BiDataSourceCostEnum biDataSourceCostEnum : enums) {
            if (biDataSourceCostEnum.getName().equals(name)) {
                return biDataSourceCostEnum.getCode();
            }
        }
        return null;
    }
}
