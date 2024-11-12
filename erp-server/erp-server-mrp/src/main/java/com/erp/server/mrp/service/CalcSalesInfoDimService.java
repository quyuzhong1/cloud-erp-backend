package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.model.mrp.entity.CalcSalesInfoDimEntity;

import java.util.List;

/**
 * <p>
 * 销量试算表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CalcSalesInfoDimService extends SuperService<CalcSalesInfoDimEntity> {


    /**
     *
     * @param calcResultList 计算参数
     */
    void calcSalesInfo(List<CalcSalesInfoDimDTO.CalcResultDTO> calcResultList);
}
