package com.erp.server.mrp.es.service;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderHistorySalesEsService {

    /**
     * 分页查询
     *
     * @param replenishmentIds 建议id
     * @param startDate        开始时间
     * @param endDate          结束时间
     * @param pageable         分页参数
     */
    Page<OrderHistorySalesEsEntity> findByReplenishmentIdInAndDateBetween(List<String> replenishmentIds, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 批量保存
     *
     * @param historyInventoryList 历史库存
     */
    void saveAll(List<OrderHistorySalesEsEntity> historyInventoryList);

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
     * 删除数据
     * @param deleteIds id
     */
    void deleteByIdIn(List<String> deleteIds);

    /**
     * 查询历史数据
     * @param suggestionIdList 建议id
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    List<ReplenishmentResultDTO.SalesHistoryDTO> listByReplenishmentIdsAndDate(List<String> suggestionIdList, String orderType, LocalDate startDate, LocalDate endDate);
}
