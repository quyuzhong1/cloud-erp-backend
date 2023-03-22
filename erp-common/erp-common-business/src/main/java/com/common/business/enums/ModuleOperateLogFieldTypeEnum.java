package com.common.business.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/22 10:59
 */
public enum ModuleOperateLogFieldTypeEnum {

    TYPE_STRING(0,"字符串"),
    TYPE_YES_NO(1,"是或否"),
    TYPE_ENUM(2,"枚举"),
    TYPE_DIST(3,"字典"),
    TYPE_USER(4,"人员");

    public Integer code;
    public String name;

    public Integer code() {
        return code;
    }
    ModuleOperateLogFieldTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        for (ModuleOperateLogFieldTypeEnum item : ModuleOperateLogFieldTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                item.getName();
            }
        }
        return "";
    }
}
