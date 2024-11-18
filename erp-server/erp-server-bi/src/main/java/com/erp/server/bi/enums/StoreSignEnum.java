package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 店铺标识
 * @date 2022/12/14 19:11
 */
public enum StoreSignEnum {

    CN("cn","国内"),
    ABROAD("abroad","国外"),
    OTHER("other","其他");

    private String code;

    private String name;


    StoreSignEnum(String code, String name) {
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
        for (StoreSignEnum state : StoreSignEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
