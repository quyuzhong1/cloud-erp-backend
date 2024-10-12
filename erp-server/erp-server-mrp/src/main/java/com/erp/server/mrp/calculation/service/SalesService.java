package com.erp.server.mrp.calculation.service;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;

import java.util.List;

public interface SalesService {

    /**
     * 查询全部销售出库单销量
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySob2c(String calculation, String orderType);
    /**
     * 查询全部销售订单销量
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySoOutStock(String calculation, String orderType);
}
