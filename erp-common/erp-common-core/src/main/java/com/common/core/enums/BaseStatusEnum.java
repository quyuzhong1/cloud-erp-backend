package com.common.core.enums;

/**
 * @Classname BaseStatusEnum1
 * @Description TODO
 * @Date 2023-02-08 18:00
 * @Created by yl
 */
public enum BaseStatusEnum {


    CHANGE_ING("changeIng","变更中"),
    CANCEL("cancel","取消"),
    WAIT_SUBMIT("waitSubmit","待提交"),
    WAIT_AUDIT("waitAudit", "待审核"),
    AUDIT_ING("auditIng", "审核中"),
    AUDIT_NO_PASS("auditNoPass", "审核不通过"),
    AUDIT_PASS("auditPass", "审核通过");

    private String status;
    private String name;

    BaseStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static String getName(String state) {
        for (BaseStatusEnum item : BaseStatusEnum.values()) {
            if (state.equals(item.getStatus())) {
                return item.getName();
            }
        }
        return "";
    }
}
