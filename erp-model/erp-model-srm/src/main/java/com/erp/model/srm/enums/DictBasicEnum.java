package com.erp.model.srm.enums;

public enum DictBasicEnum {

    CFG_SETTING("cfgSetting", "系统配置"),
    ;

    private String type;
    private String name;

    DictBasicEnum(String code, String name) {
        this.type = code;
        this.name = name;
    }

    public void setCode(String code) {
        this.type = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return type;
    }
    public String getType() {
        return name;
    }

    public static String getNameByCode(String code) {
        DictBasicEnum[] stateEnums = values();
        for (DictBasicEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getType();
            }
        }
        return "";
    }

}
