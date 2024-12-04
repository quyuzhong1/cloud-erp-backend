package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.CalcSalesInfoEstimateEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 试算销量预估 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CalcSalesInfoEstimateService extends SuperService<CalcSalesInfoEstimateEntity> {


    /**
     * 通过试算id查询预估销量
     * @param ids ids
     */
    List<CalcSalesInfoEstimateEntity> listByCalcSalesInfoIds(List<String> ids);

    /**
     * 通过试算id,日期查询预估销量
     * @param dimIds ids
     */
    List<CalcSalesInfoEstimateEntity> listByCalcSalesInfoIdsAndDate(List<String> dimIds, LocalDate startCalcDate, LocalDate endCalcDate);
}
