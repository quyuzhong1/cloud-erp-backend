package com.common.core.enums;

import java.util.Arrays;

/**
 * @author Lambda
 * @Classname DictBasicEnum
 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictEnum {


    AND("and", "且","&&"),
    OR("or", "或","||"),
    CONTAINS("contains", "包含",""),
    NOT_CONTAINS("notContains", "不包含",""),
    IS_NULL("isNull", "为空",""),
    NOT_NULL("notNull", "不为空",""),


    ;


    private String code;

    private String name;

    private String desc;


    DictEnum(String code, String name, String desc) {

        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public static String getName(String code) {
        for (DictEnum dictBasic : DictEnum.values()) {
            if (code.equals(dictBasic.getCode())) {
                return dictBasic.getName();
            }
        }
        return "";
    }

    public static String getDesc(String code) {
        for (DictEnum dictBasic : DictEnum.values()) {
            if (code.equals(dictBasic.getCode())) {
                return dictBasic.getDesc();
            }
        }
        return "";
    }

    public static DictEnum getByStatus(String code){
        return Arrays.stream(values()).filter(a -> a.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
