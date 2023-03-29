package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/27 10:52
 */
public enum PurchaseListTypeEnum {

    TO_BE_APPROVE("toBeApprove", "待审批"),
    TO_BE_CREATE("toBeCreate", "待生成"),
    CREATED("created", "已生成"),
    REJECT("reject", "不通过");


    private String code;
    private String name;

    PurchaseListTypeEnum(String code, String name) {
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
