package com.erp.model.wms.enums;

public enum PoReturnStatusEnum {
    WAIT_SUBMIT("waitSubmit", "待提交"),
    TO_BE_APPROVE("toBeApprove", "待审批"),
    APPROVE("approve", "审核通过"),
    REJECT("reject", "不通过"),
    WAIT_FOR_ME_HANDLE("waitForMeHandle", "待我处理");

    private String code;
    private String name;

    PoReturnStatusEnum(String code, String name) {
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
