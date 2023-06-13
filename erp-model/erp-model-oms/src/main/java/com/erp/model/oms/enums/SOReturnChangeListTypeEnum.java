package com.erp.model.oms.enums;

public enum SOReturnChangeListTypeEnum {
    TO_BE_APPROVE("toBeApprove", "待审批"),
    APPROVE("approve", "审核通过"),
    REJECT("reject", "不通过");


    private String code;
    private String name;

    SOReturnChangeListTypeEnum(String code, String name) {
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
