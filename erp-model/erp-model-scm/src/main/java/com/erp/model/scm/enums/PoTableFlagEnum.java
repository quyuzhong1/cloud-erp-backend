package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/27 10:52
 */
public enum PoTableFlagEnum {

    WAIT_SUBMIT("waitSubmit", "待提交"),
    TO_BE_APPROVE("toBeApprove", "待我审核"),
    TO_BE_CONFIRM("toBeConfirm", "待确认"),
    CONFIRM("confirm", "已确认"),
    REJECT("reject", "已拒绝"),
    DELIVERY("delivery", "送货中"),
    FINISH("finish", "已完成"),
    CLOSED("closed", "已关闭"),
    APPROVE_REJECT("approveReject", "不通过"),
    ;


    private String code;
    private String name;

    PoTableFlagEnum(String code, String name) {
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
