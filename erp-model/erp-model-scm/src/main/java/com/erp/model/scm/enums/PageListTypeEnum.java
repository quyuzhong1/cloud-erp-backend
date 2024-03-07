package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/27 10:52
 */
public enum PageListTypeEnum {

    WAIT_SUBMIT("waitSubmit", "待提交"),
    TO_BE_APPROVE("toBeApprove", "待审核"),
    APPROVE("approve", "审核通过"),
    REJECT("reject", "不通过");

    private String code;
    private String name;

    PageListTypeEnum(String code, String name) {
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
