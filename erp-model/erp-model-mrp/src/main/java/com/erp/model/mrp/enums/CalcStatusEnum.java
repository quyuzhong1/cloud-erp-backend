package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CalcStatusEnum implements EnumMessage {

    DOING("doing", "进行中"),
    FINISH("finish", "已完成");

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

    public static String getNameByCode(String code) {
        for (CalcStatusEnum typeEnum : CalcStatusEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum.getName();
            }
        }
        throw new ServiceException(ApiError.ERROR_9028);
    }
}
