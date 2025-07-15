package com.erp.model.scm.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author jack
 * @Classname ContractStampStatusEnum
 * @Date 2025-05-12
 */
public enum ContractStampStatusEnum {
    WAIT_SUBMIT("waitSubmit", "待申请"),
    APPROVE_ING("approveIng", "已申请"),
    APPROVE("approve", "已完成")
    ;

    private String code;
    private String name;


    ContractStampStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (ContractStampStatusEnum item : ContractStampStatusEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }
}
