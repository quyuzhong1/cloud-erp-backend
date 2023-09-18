package com.erp.server.bi.service.impl;


import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.server.bi.service.ListYearMonthValueStrategy;

import java.util.List;

/**
 * @Description 年月目标值上下文
 * @Author yl
 * @Date 2023-09-18 15:26
 */
public class YearMonthValueContext {

    private ListYearMonthValueStrategy yearMonthValueStrategy;

    public YearMonthValueContext(ListYearMonthValueStrategy yearMonthValueStrategy) {
        this.yearMonthValueStrategy = yearMonthValueStrategy;
    }

    public List<BiTargetYearDTO.YearMonthValueDTO> listMetricsValue(Integer year, String metrics, String flagId) {
        return yearMonthValueStrategy.ListYearMonthValue(year, metrics, flagId);
    }
}
