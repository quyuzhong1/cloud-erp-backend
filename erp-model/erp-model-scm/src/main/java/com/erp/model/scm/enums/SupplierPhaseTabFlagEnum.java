package com.erp.model.scm.enums;

import com.common.core.constant.EnumMessage;

/**
 * @author Will
 * @version 1.0
 */
public enum SupplierPhaseTabFlagEnum implements EnumMessage {

    //待我审核
    APPROVE_ING("approveIng", "待我审核"),
    //已审核
    APPROVE("approve", "已审核"),
    //不通过
    REJECT("reject", "不通过"),
    ;


    private String code;
    private String name;

    SupplierPhaseTabFlagEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        SupplierPhaseTabFlagEnum[] stateEnums = values();
        for (SupplierPhaseTabFlagEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
