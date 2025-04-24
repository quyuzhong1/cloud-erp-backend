package com.erp.server.mrp.es.service;

import cn.hutool.json.JSONArray;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OutStockHistorySalesEsService {


    /**
     * 保存数据
     * @param outStockHistorySales 销售出库单历史销量
     */
    void saveAll(List<OutStockHistorySalesEsEntity> outStockHistorySales);

    /**
     * 根据建议id分组
     *
     * @param shopSkuIds       shopId-skuId
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    Map<String, Integer> countQtyByShopSkuIdsAndDate(List<String> shopSkuIds, JSONArray orderType, LocalDate startDate, LocalDate endDate);

    /**
     * 查询历史数据
     * @param shopSkuIds       shopId-skuId
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    List<ReplenishmentResultDTO.SalesHistoryDTO> listByShopSkuIdsAndDate(List<String> shopSkuIds, JSONArray orderType, LocalDate startDate, LocalDate endDate);

    /**
     * 根据店铺查询有销量sku
     *
     * @param shopIds 店铺
     */
    Map<String, Set<String>> listSkuByShopId(Set<String> shopIds);

    /**
     * 删除历史数据
     *
     * @param shopSkuIds 店铺skuid
     * @param startDate  开始日期
     * @param endDate    结束日期
     */
    void deleteByDateBetween(List<String> shopSkuIds, LocalDate startDate, LocalDate endDate);
}
