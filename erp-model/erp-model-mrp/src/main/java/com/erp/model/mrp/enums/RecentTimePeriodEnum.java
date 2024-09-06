package com.erp.model.mrp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
