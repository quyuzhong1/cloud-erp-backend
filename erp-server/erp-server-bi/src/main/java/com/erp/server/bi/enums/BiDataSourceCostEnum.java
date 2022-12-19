package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 11:38
 */
public enum BiDataSourceCostEnum {

    NEWREFUND(1, "新建退款"),
    UNDERREVIEW(2,"审核中"),
    FINANCIALREVIEW(3, "财务审核"),
    SUCCESS(4, "成功"),
    FAIL(5, "失败"),
    VOIDED(6, "作废");

    private Integer code;
    private String name;

    BiDataSourceCostEnum(Integer code, String name) {
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
        for (BiDataSourceCostEnum biDataSourceCostEnum : BiDataSourceCostEnum.values()) {
            if (code.equals(biDataSourceCostEnum.getCode())) {
                return biDataSourceCostEnum.getName();
            }
        }
        return "";
    }

    public static Integer getCodeByName(String name) {
        BiDataSourceCostEnum[] enums = values();
        for (BiDataSourceCostEnum biDataSourceCostEnum : enums) {
            if (biDataSourceCostEnum.getName().equals(name)) {
                return biDataSourceCostEnum.getCode();
            }
        }
        return null;
    }
}
