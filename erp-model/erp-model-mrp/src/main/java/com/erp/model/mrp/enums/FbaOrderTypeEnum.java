package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;

public enum FbaOrderTypeEnum implements EnumMessage {
    //fba
    FBA("fba", "FBA"),
    //fbm
    FBM("fbm", "FBM");


    private String code;
    private String name;

    FbaOrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    @Override
    public String getCode() {
        return code;
    }
    @Override
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        FbaOrderTypeEnum[] stateEnums = values();
        for (FbaOrderTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
