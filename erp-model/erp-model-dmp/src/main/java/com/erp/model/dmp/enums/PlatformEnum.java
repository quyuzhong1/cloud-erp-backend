package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 18:41
 */
public enum PlatformEnum {

    MABANG(0, "mabang", "马帮"),
    GYY(1, "gyy", "管易云"),
    KINGDEE(2, "kingdee", "金蝶云星空");

    private Integer code;

    private String name;

    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    PlatformEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static PlatformEnum getByCode(Integer code) {
        PlatformEnum[] values = values();
        for (PlatformEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static PlatformEnum getByName(String name) {
        PlatformEnum[] values = values();
        for (PlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
