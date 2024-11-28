package com.erp.server.mrp.es.repository;

import com.erp.server.mrp.es.entity.HistoryInventoryEsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HistoryInventoryEsRepository extends ElasticsearchRepository<HistoryInventoryEsEntity, String> {

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
     * 查询历史库存，根据建议id
     *
     * @param replenishmentId 建议id
     * @param startDate       开始日期
     * @param endDate         结束日期
     */
    List<HistoryInventoryEsEntity> findByReplenishmentIdAndDateBetween(String replenishmentId, LocalDate startDate, LocalDate endDate);


    /**
     * 根据开始结束时间，建议id删除数据
     *
     * @param replenishmentIds 建议
     * @param startDate        开始时间
     * @param endDate          结束时间
     */
    void deleteByReplenishmentIdInAndDateBetween(List<String> replenishmentIds, LocalDate startDate, LocalDate endDate);
}
