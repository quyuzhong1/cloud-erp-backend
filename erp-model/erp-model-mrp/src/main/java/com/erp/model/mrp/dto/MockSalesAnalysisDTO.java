package com.erp.model.mrp.dto;

import com.erp.model.mrp.enums.TimePeriodEstimateEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import java.time.LocalDate;

@Getter
@Setter
public class MockSalesAnalysisDTO {

    /**
     * 主表id
     */
    private String mainId;
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
     * @see TimePeriodEstimateEnum
     */
    private String timePeriod;

    /**
     * 销量设置
     */
    @Valid
    private CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO;

}
