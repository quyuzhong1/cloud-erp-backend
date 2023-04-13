package com.erp.model.wms.enums;

public enum SysClassifyEnum {
    PLM("PLM", "PLM系统"),
    SCM("SCM", "SCM系统"),
    WMS("WMS", "WMS系统");

    private String code;
    private String name;

    SysClassifyEnum(String code, String name) {
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
        for (SysClassifyEnum state : SysClassifyEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
