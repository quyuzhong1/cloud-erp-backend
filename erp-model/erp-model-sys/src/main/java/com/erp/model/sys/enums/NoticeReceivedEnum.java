package com.erp.model.sys.enums;

/**
 * @Classname NoticeEnum
 * @Description TODO
 * @Date 2022-11-11 10:57
 * @Created by yl
 */
public enum NoticeReceivedEnum {

    NEW_TASK("itemRole", "产品经理","productCharge");

    private String code;
    private String name;
    private String value;

    NoticeReceivedEnum(String code, String name,String value) {
        this.code = code;
        this.name = name;
        this.name = value;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }



}
