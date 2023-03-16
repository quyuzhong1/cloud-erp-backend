package com.common.business.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 17:28
 */
public enum ApproveStatusEnum {

    WAIT_SUBMIT("waitSubmit", "待提交"),
    AUDIT_ING("auditIng", "审核中"),
    AUDIT_NO_PASS("auditNoPass", "审核不通过"),
    FINISH("finish", "已完成");

    private String status;
    private String name;

    ApproveStatusEnum(String status, String name) {
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
        if (StringUtils.isNotBlank(state)) {
            for (ApproveStatusEnum item : ApproveStatusEnum.values()) {
                if (state.equals(item.getStatus())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
