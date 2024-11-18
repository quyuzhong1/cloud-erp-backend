package com.common.business.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/22 10:59
 */
public enum ModuleOperateLogFieldTypeEnum {

    TYPE_STRING(0,"字符串"),
    TYPE_YES_NO(1,"是或否"),
    TYPE_ENUM(2,"枚举"),
    TYPE_DIST(3,"字典"),
    TYPE_USER(4,"人员"),
    TYPE_COUNTRY(5,"国家"),
    TYPE_DEPT(6,"部门"),
    TYPE_CITY(7,"城市"),
    TYPE_CUSTOMER(8,"客户"),
    TYPE_CURRENCY(9,"币别")
    ;

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
                return item.getName();
            }
        }
        return "";
    }
    public static ModuleOperateLogFieldTypeEnum getEnumByCode(Integer code) {
        for (ModuleOperateLogFieldTypeEnum item : ModuleOperateLogFieldTypeEnum.values()) {
            if (item.getCode().equals(code)) {
               return item;
            }
        }
        return null;
    }
}
