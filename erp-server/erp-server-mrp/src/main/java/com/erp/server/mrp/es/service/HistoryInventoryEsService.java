package com.erp.server.mrp.es.service;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.es.entity.HistoryInventoryEsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HistoryInventoryEsService {
    /**
     * 分页查询
     *
     * @param replenishmentIds 建议id
     * @param startDate        开始时间
     * @param endDate          结束时间
     * @param pageable         分页参数
     */
    Page<HistoryInventoryEsEntity> findByReplenishmentIdInAndDateBetween(List<String> replenishmentIds, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 批量保存
     * @param historyInventoryList 历史库存
     */
    void saveAll(List<HistoryInventoryEsEntity> historyInventoryList);


    /**
     * 查询历史库存，根据建议id
     *
     * @param replenishmentId 建议id
     * @param startDate       开始日期
     * @param endDate         结束日期
     */
    List<HistoryInventoryEsEntity> findByReplenishmentIdAndDateBetween(String replenishmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询历史库存，根据建议id
     *
     * @param replenishmentId 建议id
     * @param startDate       开始日期
     * @param endDate         结束日期
     */
    Map<LocalDate, Integer> findByReplenishmentIdAndDateBetweenMap(String replenishmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 删除数据
     * @param ids ids
     */
    void deleteByIdIn(List<String> ids);


    /**
     * 查询历史库存，根据建议id
     *
     * @param suggestionIds 建议id
     * @param startDate       开始日期
     * @param endDate         结束日期
     */
    List<ReplenishmentResultDTO.InventoryHistoryDTO> listByReplenishmentIdsAndDate(List<String> suggestionIds, LocalDate startDate, LocalDate endDate);
}
