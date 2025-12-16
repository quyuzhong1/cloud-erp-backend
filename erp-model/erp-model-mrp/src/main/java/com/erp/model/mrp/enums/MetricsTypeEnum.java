package com.erp.model.mrp.enums;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MetricsTypeEnum {
    MAE,
    MSE,
    RMSE,
    MAPE,
    R2;

    public static MetricsTypeEnum getEnum(String code) {
        for (MetricsTypeEnum typeEnum : MetricsTypeEnum.values()) {
            if (code.equals(typeEnum.name())) {
                return typeEnum;
            }
        }
        throw new ServiceException(ApiError.COMMON_ENUM_CONVERT_FAILED);
    }
}
