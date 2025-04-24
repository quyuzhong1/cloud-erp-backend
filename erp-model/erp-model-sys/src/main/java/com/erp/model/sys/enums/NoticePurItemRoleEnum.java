package com.erp.model.sys.enums;

/**
 * @author jack
 * @Classname NoticePurItemRoleEnum
 * @Date 2025-03-13
 */
public enum NoticePurItemRoleEnum {

    CREATE_USER("createUser", "创建人"),
    APPROVE_USER("approveUser", "审核人"),
    PURCHASER("purchaser", "采购员"),
    ;


    private String code;
    private String name;

    NoticePurItemRoleEnum(String code, String name) {
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
