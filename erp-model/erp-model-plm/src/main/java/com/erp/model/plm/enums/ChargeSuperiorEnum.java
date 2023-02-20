package com.erp.model.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/13 18:55
 */
public enum ChargeSuperiorEnum {

    DIRECT_SUPERIOR(0,"direct_superior","直属上级"),
    DIRECT_DEPARTMENT_CHARGE(1,"direct_department_charge","直接部门负责人"),
    SECOND_DEPARTMENT_CHARGE(2,"second_department_charge","二级部门负责人"),
    THREE_DEPARTMENT_CHARGE(3,"three_department_charge","三级部门负责人"),
    FOUR_DEPARTMENT_CHARGE(4,"four_department_charge","四级部门负责人"),
    FIVE_DEPARTMENT_CHARGE(5,"five_department_charge","五级部门负责人");

    private Integer code;

    private String name;

    private String desc;

    ChargeSuperiorEnum(Integer code, String name ,String desc) {
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
        for (ChargeSuperiorEnum chargeSuperiorEnum : ChargeSuperiorEnum.values()) {
            if (chargeSuperiorEnum.getCode().equals(code)) {
                return chargeSuperiorEnum.getName();
            }
        }
        return "";
    }
    public static String getDesc(String name) {
        for (ChargeSuperiorEnum chargeSuperiorEnum : ChargeSuperiorEnum.values()) {
            if (chargeSuperiorEnum.getName().equals(name)) {
                return chargeSuperiorEnum.getDesc();
            }
        }
        return "";
    }
}
