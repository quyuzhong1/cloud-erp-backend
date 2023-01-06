package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 11:38
 */
public enum BiDataSourceCostEnum {

    MONTH("month", "月份","*月份"),
    DEPTNAME("deptName","销售事业部","*销售事业部"),
    PLATFORMNAME("platformName", "平台名称", "*平台名称"),
    SITE("site", "站点", "*站点"),
    SHOPNAME("shopName", "店铺名称", "*店铺名称"),
    CHARGENAME("chargeName", "负责人", "*负责人"),
    COMBINATION("combination", "组合", "组合");

    private String code;
    private String name;
    private String desc;

    BiDataSourceCostEnum(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public static String getName(String code) {
        for (BiDataSourceCostEnum biDataSourceCostEnum : BiDataSourceCostEnum.values()) {
            if (biDataSourceCostEnum.getCode().equals(code)) {
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
