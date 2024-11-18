package com.erp.model.sys.enums;

/**
 * @author Lambda
 * @Classname DictValueEnum
 * @Date 2023-09-05 16:58
 * @Created by yl
 */
public enum DictValueEnum {

    GL("GL","全球");


    private String code;

    private String name;




    DictValueEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
