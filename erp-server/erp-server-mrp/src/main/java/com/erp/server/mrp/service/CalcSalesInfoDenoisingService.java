package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.CalcSalesInfoDenoisingEntity;

import java.util.List;

/**
 * <p>
 * 试算销量去噪 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CalcSalesInfoDenoisingService extends SuperService<CalcSalesInfoDenoisingEntity> {


    /**
     * 根据试算id查询去噪销量
     * @param calcSalesInfoId 试算id
     */
    List<CalcSalesInfoDenoisingEntity> listByCalcSalesInfoId(String calcSalesInfoId);
}
