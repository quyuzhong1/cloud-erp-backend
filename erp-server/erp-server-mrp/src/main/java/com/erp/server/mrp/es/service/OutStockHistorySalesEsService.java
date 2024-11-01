package com.erp.server.mrp.es.service;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OutStockHistorySalesEsService {

    /**
     * 分页查询
     * @param ids 建议id
     * @param startDate 开始时间
     * @param endDate 结束时间
     */
    Page<OutStockHistorySalesEsEntity> findByReplenishmentIdInAndDateBetween(List<String> ids, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 保存数据
     * @param outStockHistorySales 销售出库单历史销量
     */
    void saveAll(List<OutStockHistorySalesEsEntity> outStockHistorySales);

    /**
     * 根据建议id分组
     *
     * @param replenishmentIds 建议id
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    Map<String, Integer> countQtyByReplenishmentIdsAndDate(List<String> replenishmentIds, String orderType, LocalDate startDate, LocalDate endDate);

    /**
     * 查询历史数据
     * @param suggestionIdList 建议id
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    List<ReplenishmentResultDTO.SalesHistoryDTO> listByReplenishmentIdsAndDate(List<String> suggestionIdList, String orderType, LocalDate startDate, LocalDate endDate);

    /**
     * 删除原数据
     * @param suggestionIds 建议id
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    void deleteBySuggestionIdsAndDate(List<String> suggestionIds, LocalDate startDate, LocalDate endDate);
}
