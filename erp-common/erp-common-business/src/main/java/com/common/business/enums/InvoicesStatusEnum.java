package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Lambda
 */
public enum InvoicesStatusEnum implements EnumMessage {
    NOT_SENT("notSent", "未发送"),
    SENT("sent", "已发送"),
    ;

    private String code;

    private String name;

    InvoicesStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (InvoicesStatusEnum item : InvoicesStatusEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
