package com.erp.model.sys.enums;

/**
 * @author Lambda
 * @Classname NoticeItemRoleEnum
 * @Description TODO
 * @Date 2023-04-28 12:00
 * @Created by yl
 */
public enum NoticeItemRoleEnum {

    ITEM_MANAGER("projectCharge", "项目经理"),
    PRODUCT_MANAGER("productCharge", "产品经理");


    private String code;
    private String name;

    NoticeItemRoleEnum(String code, String name) {
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
