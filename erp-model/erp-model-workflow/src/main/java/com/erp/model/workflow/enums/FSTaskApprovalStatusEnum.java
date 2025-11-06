package com.erp.model.workflow.enums;

public enum FSTaskApprovalStatusEnum {
    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "任务同意"),
    REJECTED("REJECTED", "任务拒绝"),
    TRANSFERRED("TRANSFERRED", "任务转交"),
    CANCELED("CANCELED", "撤销"),
    DELETED("DONE", "任务通过但审批人未操作。审批人看不到该任务时，如需查看可抄送至该审批人"),
    ;

    private final String code;
    private final String description;

    FSTaskApprovalStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
    public static FSTaskApprovalStatusEnum getByCode(String code) {
        for (FSTaskApprovalStatusEnum value : FSTaskApprovalStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
