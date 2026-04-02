package com.erp.model.oms.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * B2C寄样单据状态
 */
public enum KolB2cApplicationDocumentStatusEnum {
    WAIT("wait", "未创建"),
    CREATED("created", "已创建"),
    CANCELING("canceling", "取消中"),
    CANCELED("canceled", "已取消"),
    CANCEL_FAIL("cancelFail", "取消失败");

    private final String code;
    private final String name;

    KolB2cApplicationDocumentStatusEnum(String code, String name) {
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
        if (StringUtils.isBlank(code)) {
            return WAIT.getName();
        }
        for (KolB2cApplicationDocumentStatusEnum statusEnum : values()) {
            if (StringUtils.equals(code, statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return WAIT.getName();
    }

    public static String normalize(String code) {
        if (StringUtils.isBlank(code)) {
            return WAIT.getCode();
        }
        for (KolB2cApplicationDocumentStatusEnum statusEnum : values()) {
            if (StringUtils.equals(code, statusEnum.getCode())) {
                return statusEnum.getCode();
            }
        }
        return WAIT.getCode();
    }
}
