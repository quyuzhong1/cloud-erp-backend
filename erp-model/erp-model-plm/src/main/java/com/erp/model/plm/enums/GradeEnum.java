package com.erp.model.plm.enums;

import com.common.core.constant.EnumMessage;

/**
 * 产品等级
 */
public enum GradeEnum implements EnumMessage {
    A("A", "A"),
    B("B", "B"),
    C("C", "C"),
    D("D", "D"),
    S("S", "S"),
    UNKNOWN("未知", "未知");

    private String code;
    private String name;

    GradeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        GradeEnum[] gradeEnums = values();
        for (GradeEnum gradeEnum : gradeEnums) {
            if (gradeEnum.getCode().equals(code)) {
                return gradeEnum.getName();
            }
        }
        return null;
    }

    public static GradeEnum getEnumByType(String code) {
        GradeEnum[] gradeEnums = values();
        for (GradeEnum gradeEnum : gradeEnums) {
            if (gradeEnum.getCode().equals(code)) {
                return gradeEnum;
            }
        }
        return null;
    }

    public static String getCodeByName(String name) {
        GradeEnum[] gradeEnums = values();
        for (GradeEnum gradeEnum : gradeEnums) {
            if (gradeEnum.getName().equals(name)) {
                return gradeEnum.getCode();
            }
        }
        return null;
    }
}