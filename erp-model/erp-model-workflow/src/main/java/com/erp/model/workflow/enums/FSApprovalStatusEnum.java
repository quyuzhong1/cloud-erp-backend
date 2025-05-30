package com.erp.model.workflow.enums;

public enum FSApprovalStatusEnum {
    PENDING("PENDING", "审批中"),
    APPROVED("APPROVED", "审批流程结束，结果为同意"),
    REJECTED("REJECTED", "审批流程结束，结果为拒绝"),
    CANCELED("CANCELED", "审批发起人撤回"),
    DELETED("DELETED", "审批被删除"),
    HIDDEN("HIDDEN", "状态隐藏（不显示状态）"),
    TERMINATED("TERMINATED", "审批终止"),
    OVERTIME_CLOSE("OVERTIME_CLOSE", "审批超时关闭"),
    OVERTIME_RECOVER("OVERTIME_RECOVER", "审批超时恢复")
    ;

    private final String code;
    private final String description;

    FSApprovalStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
    public static FSApprovalStatusEnum getByCode(String code) {
        for (FSApprovalStatusEnum value : FSApprovalStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
