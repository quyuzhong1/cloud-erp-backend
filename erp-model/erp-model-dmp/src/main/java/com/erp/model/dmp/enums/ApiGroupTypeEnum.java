package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/10 9:46
 */
public enum ApiGroupTypeEnum {

    NORMAL("0", "正常级别"),
    PARENT("1", "集合父项"),
    CHILD("2", "集合子项");

    private String code;

    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    ApiGroupTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
