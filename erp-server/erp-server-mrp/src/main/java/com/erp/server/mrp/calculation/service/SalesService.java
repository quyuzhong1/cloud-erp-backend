package com.erp.server.mrp.calculation.service;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;

import java.util.List;

public interface SalesService {
    List<ReplenishmentResultDTO.SalesInfoDTO> listSalesBySob2c(ReplenishmentResultDTO replenishmentResult);

    List<ReplenishmentResultDTO.SalesInfoDTO> listSalesBySoOutStock(ReplenishmentResultDTO replenishmentResult);

    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllSalesBySoOutStock(String calculation);

    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllSalesBySob2c(String calculation);
}
