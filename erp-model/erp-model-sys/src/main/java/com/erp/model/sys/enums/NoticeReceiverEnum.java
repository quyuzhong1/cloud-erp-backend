package com.erp.model.sys.enums;

/**
 * @Classname NoticeEnum
 * @Description TODO
 * @Date 2022-11-11 10:57
 * @Created by yl
 */
public enum NoticeReceiverEnum {

    ITEM_ROLE("itemRole", "项目角色"),
    OTHER_PEOPLE("otherPeople", "其它人员");

    private String code;
    private String name;

    NoticeReceiverEnum(String code, String name) {
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
