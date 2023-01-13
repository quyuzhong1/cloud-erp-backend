package com.erp.server.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/13 18:55
 */
public enum ChargeSuperiorEnum {

    DIRECT_SUPERIOR(0,"直属上级"),
    DIRECT_DEPARTMENT_CHARGE(1,"直接部门负责人"),
    SECOND_DEPARTMENT_CHARGE(2,"二级部门负责人"),
    THREE_DEPARTMENT_CHARGE(3,"三级部门负责人"),
    FOUR_DEPARTMENT_CHARGE(4,"四级部门负责人"),
    FIVE_DEPARTMENT_CHARGE(5,"五级部门负责人");

    private Integer code;

    private String name;


    ChargeSuperiorEnum(Integer code, String name) {
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
        for (ChargeSuperiorEnum state : ChargeSuperiorEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
