package com.erp.server.plm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 系统编码
 * @date 2022/11/22 10:26
 */
public enum SysNoEnum {

    SKU_NO(1, "sku_no");

    private Integer code;
    private String name;

    SysNoEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        SysNoEnum[] sysNoEnums = values();
        for (SysNoEnum sysNoEnum : sysNoEnums) {
            if (sysNoEnum.getCode() == code) {
                return sysNoEnum.getName();
            }
        }
        return null;
    }

    public static SysNoEnum getEnumByType(String code){
        SysNoEnum[] sysNoEnums = values();
        for (SysNoEnum sysNoEnum : sysNoEnums) {
            if (sysNoEnum.getCode().equals(code)) {
                return sysNoEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        SysNoEnum[] sysNoEnums = values();
        for (SysNoEnum sysNoEnum : sysNoEnums) {
            if (sysNoEnum.getName().equals(name)) {
                return sysNoEnum.getCode();
            }
        }
        return null;
    }
}
