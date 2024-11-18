package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum TimePeriodEnum implements EnumMessage {

    THREE("THREE", "3天", 3),

    SEVEN("SEVEN", "7天", 7),

    FOURTEEN("FOURTEEN", "14天", 14),

    THIRTY("THIRTY", "30天", 30),

    SIXTY("SIXTY", "60天", 60),

    NINETY("NINETY", "90天", 90),

    ONE_HUNDRED_AND_EIGHTY("ONE_HUNDRED_AND_EIGHTY", "180天", 180),

    TWO_HUNDRED_AND_SEVENTY("TWO_HUNDRED_AND_SEVENTY", "270天", 270),

    THREE_HUNDRED_AND_SIXTY("THREE_HUNDRED_AND_SIXTY", "360天", 360);

    private final String code;

    private final String name;

    private final Integer days;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static TimePeriodEnum of(String code) {
        return Arrays.stream(TimePeriodEnum.values()).filter(v -> v.getCode().equals(code))
                .findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST, "时间段类型"));
    }
}
