package com.erp.model.tms.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum ExceptionTypeEnum implements EnumMessage {

    ORDER_EXCEPTION("orderException", "单据异常"),
    LABEL_EXCEPTION("labelException", "面单异常"),
    ;

    private final String code;

    private final String name;

    ExceptionTypeEnum(String code, String name) {
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
        for (ExceptionTypeEnum item : ExceptionTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item.getName();
            }
        }
        return "";
    }
}
