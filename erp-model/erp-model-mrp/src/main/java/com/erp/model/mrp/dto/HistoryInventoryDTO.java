package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class HistoryInventoryDTO {

    /**
     * 明细id
     */
    private String detailId;
    /**
     * 开始日期
     */
    private LocalDate startDate  = LocalDate.now().minusDays(90);
    /**
     * 结束日期
     */
    private LocalDate endDate = LocalDate.now();
}
