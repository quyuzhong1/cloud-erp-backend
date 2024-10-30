package com.erp.server.mrp.calculation.service;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;

import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;

public interface SalesService {

    /**
     * 查询全部销售出库单销量
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySob2c(LocalDate calculationDate, Integer cleanDay);
    /**
     * 查询全部销售订单销量
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySoOutStock(LocalDate calculationDate, Integer cleanDay);

    /**
     * 获取海外仓销量数据
     * @param calcDate 计算日
     * @param orderType 单据类型
     * @param channelIdList 海外仓渠道
     * @param platforms 平台
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllOverseasSalesBySob2c(String calcDate, String orderType, List<String> channelIdList, List<String> platforms);

    /**
     * 获取海外仓销量数据
     * @param calcDate 计算日
     * @param orderType 单据类型
     * @param channelIdList 海外仓渠道
     * @param platforms 平台
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllOverseasSalesBySoOutStock(String calcDate, String orderType, List<String> channelIdList, List<String> platforms);
}
