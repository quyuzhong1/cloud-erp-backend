package com.erp.model.srm.enums;

/**
 * @author zdy
 * @version 1.0

 * @date 2023/3/27 19:13
 */
public enum TimeUtilEnum {

    hour("H", "小时"),
    minute("M", "分钟"),
    second("S", "秒"),
    ;

    private String code;
    private String name;

    TimeUtilEnum(String code, String name) {
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
        TimeUtilEnum[] stateEnums = values();
        for (TimeUtilEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
    public static TimeUtilEnum getEnum(String code) {
        for (TimeUtilEnum settingEnum : TimeUtilEnum.values()) {
            if (code.equals(settingEnum.getCode())) {
                return settingEnum;
            }
        }
        return null;
    }
}
