package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplyTypeEnum implements EnumMessage {
    CURRENT("CURRENT", "仅当前任务规则"),
    ALL("ALL", "模板下的全部任务规则"),
    CUSTOM("CUSTOM", "自定义");

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

    public static ApplyTypeEnum getEnum(String code) {
        for (ApplyTypeEnum typeEnum : ApplyTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        throw new ServiceException(ApiError.ERROR_ENUM_CONVERT_FAILED);
    }
}
