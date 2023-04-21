package com.erp.model.sys.enums;

/**
 * @Classname NoticeEnum
 * @Description TODO
 * @Date 2022-11-11 10:57
 * @Created by yl
 */
public enum NoticeReceivedEnum {

    PRODUCT_CHARGE("itemRole", "产品经理","productCharge"),
    PROJECT_CHARGE("itemRole", "项目经理","projectCharge"),
    OTHER_PEOPLE("otherPeople", "其它人员","projectCharge");



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
