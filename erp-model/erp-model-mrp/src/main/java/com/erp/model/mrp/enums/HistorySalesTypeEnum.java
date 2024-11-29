package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HistorySalesTypeEnum implements EnumMessage {

    SYSTEM("system", "系统"),
    CUSTOM("custom", "自定义");

    private final String code;

    private final String message;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return message;
    }
}
