package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;

public enum OrderTypeEnum implements EnumMessage {
    ALL("all", "全部"),
    //fba
    FBA("fba", "FBA"),
    //fbm
    FBM("fbm", "FBM"),
    //海外仓发货
    OVERSEAS_WAREHOUSE("overseasWarehouse", "海外仓发货"),
    //本地自发货
    LOCAL_WAREHOUSE("localWarehouse", "本地自发货");


    private String code;
    private String name;

    OrderTypeEnum(String code, String name) {
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
        OrderTypeEnum[] stateEnums = values();
        for (OrderTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
