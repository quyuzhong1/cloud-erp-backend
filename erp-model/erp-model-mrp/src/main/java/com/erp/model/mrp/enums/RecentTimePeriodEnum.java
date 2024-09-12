package com.erp.model.mrp.enums;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public enum RecentTimePeriodEnum {
    /**
     * 备货期
     */
    STOCKING_DATE,
    /**
     * 当前月
     */
    CURRENT_MONTH,
    /**
     * 下月
     */
    NEXT_MONTH,
    /**
     * 下下月
     */
    FOLLOWING_MONTH;

    public static String getNameByCode(RecentTimePeriodEnum recentTimePeriod, boolean isAvg, String calcDate) {
        LocalDate date = LocalDate.parse(calcDate, DateTimeFormatter.BASIC_ISO_DATE);
        switch (recentTimePeriod) {
            case STOCKING_DATE:
                return  isAvg ? "备货期日均" : "备货期销量";
            case CURRENT_MONTH:
                return date.getMonthValue() + "月";
            case NEXT_MONTH:
                return date.plusMonths(1).getMonthValue() + "月";
            case FOLLOWING_MONTH:
                return date.plusMonths(2).getMonthValue() + "月";
            default:
                throw new ServiceException(ApiError.ERROR_9028);
        }
    }

}
