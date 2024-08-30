package com.erp.model.mrp.dto;

import com.erp.model.mrp.enums.TimePeriodEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SalesAnalysisDTO {

    /**
     * 明细id
     */
    private String detailId;
    /**
     * 开始日期
     */
    private LocalDate startDate;
    /**
     * 结束日期
     */
    private LocalDate endDate;
    /**
     * 时间段
     * @see TimePeriodEnum
     */
    private String timePeriod;
}
