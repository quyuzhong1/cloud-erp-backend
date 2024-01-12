package com.erp.model.srm.enums;

public enum DictBasicEnum {

    CFG_SETTING("cfgSetting", "系统配置"),
    ;

    private String type;
    private String name;

    DictBasicEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public void setCode(String code) {
        this.type = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String type) {
        DictBasicEnum[] stateEnums = values();
        for (DictBasicEnum stateEnum : stateEnums) {
            if (stateEnum.getType().equals(type) ) {
                return stateEnum.getType();
            }
        }
        return "";
    }

}
