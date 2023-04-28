package com.erp.model.sys.enums;

import java.util.Arrays;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/13 18:55
 */
public enum ChargeSuperiorEnum {

    DIRECT_SUPERIOR(0,"direct_superior","直属上级", ""),
    DIRECT_DEPARTMENT_CHARGE(1,"direct_department_charge","直接部门负责人", "directSupervisor"),
    SECOND_DEPARTMENT_CHARGE(2,"second_department_charge","二级部门负责人", "secondLevelSupervisor"),
    THREE_DEPARTMENT_CHARGE(3,"three_department_charge","三级部门负责人", "thirdLevelSupervisor"),
    FOUR_DEPARTMENT_CHARGE(4,"four_department_charge","四级部门负责人", "fourthLevelSupervisor"),
    FIVE_DEPARTMENT_CHARGE(5,"five_department_charge","五级部门负责人", "fifthLevelSupervisor"),
    SIX_DEPARTMENT_CHARGE(6,"six_department_charge","六级部门负责人", "sixthLevelSupervisor"),
    SEVEN_DEPARTMENT_CHARGE(7,"seven_department_charge","七级部门负责人", "seventhLevelSupervisor"),
    EIGHT_DEPARTMENT_CHARGE(8,"eight_department_charge","八级部门负责人", "eighthLevelSupervisor"),

    ;

    private Integer code;

    private String name;

    private String desc;

    private String dictValue;

    ChargeSuperiorEnum(Integer code, String name ,String desc, String dictValue) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.dictValue = dictValue;
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

    public String getDictValue() {
        return dictValue;
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
    public static ChargeSuperiorEnum getByDictValue(String dictValue) {
        return Arrays.stream(ChargeSuperiorEnum.values())
                .filter(chargeSuperiorEnum -> dictValue.equals(chargeSuperiorEnum.getDictValue()))
                .findFirst()
                .orElse(null);
    }
}
