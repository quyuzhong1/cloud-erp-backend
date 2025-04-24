package com.erp.model.mrp.enums;

import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum TimePeriodEstimateEnum implements EnumMessage {

    /**
     * 30天
     */
    THIRTY("THIRTY", "30天", 30),

    /**
     * 60天
     */
    SIXTY("SIXTY", "60天", 60),

    /**
     * 90天
     */
    NINETY("NINETY", "90天", 90),

    /**
     * 120天
     */
    ONE_HUNDRED_AND_TWENTY("ONE_HUNDRED_AND_TWENTY", "120天", 120);


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

    public static TimePeriodEstimateEnum of(String code) {
        return Arrays.stream(TimePeriodEstimateEnum.values()).filter(v -> v.getCode().equals(code))
                .findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST, "时间段类型"));
    }
}
