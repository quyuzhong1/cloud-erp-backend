package com.erp.server.mrp.es.repository;

import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderHistorySalesEsRepository extends ElasticsearchRepository<OrderHistorySalesEsEntity, String> {

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
     * 删除数据
     *
     * @param deleteIds id
     */
    void deleteByIdIn(List<String> deleteIds);

    /**
     * 分页查询
     *
     * @param replenishmentIds 建议id
     * @param orderType        订单类型
     * @param startDate        开始时间
     * @param endDate          结束时间
     * @param pageable         分页参数
     */
    Page<OrderHistorySalesEsEntity> findByReplenishmentIdInAndOrderTypeAndDateBetween(List<String> replenishmentIds, String orderType, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
