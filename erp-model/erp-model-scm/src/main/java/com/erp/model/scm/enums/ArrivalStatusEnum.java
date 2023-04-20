package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/27 19:13
 */
public enum ArrivalStatusEnum {

    NON_ARRIVAL("0", "未到货"),
    PARTIAL_ARRIVAL("1", "部分到货"),
    ARRIVED("2", "已到货");

    private String code;
    private String name;

    ArrivalStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        ArrivalStatusEnum[] stateEnums = values();
        for (ArrivalStatusEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }

}
