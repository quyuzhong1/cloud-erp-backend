package com.erp.server.file.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileTaskTypeEnum implements EnumMessage {
    AUTO("auto","自动"),
    MANUAL("manual","手动");

    private final String code;
    private final String name;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
