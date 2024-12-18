package com.erp.server.mrp.es.service;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @param orderHistorySales 历史库存
     */
    void saveAll(List<OrderHistorySalesEsEntity> orderHistorySales);

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


    List<OrderHistorySalesEsEntity> getRecentSalesBySuggestionIds(Set<String> suggestionIds, String orderType);

    /**
     * 删除原数据
     * @param suggestionIds 建议id
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    void deleteBySuggestionIdsAndDate(List<String> suggestionIds, LocalDate startDate, LocalDate endDate);

    /**
     * 获取最近有销量数据
     * @param orderType 订单类型
     */
    Map<String, Integer> listByType(String orderType);


    /**
     * 根据sku和店铺id查询一个范围内的销量
     *
     * @param skuIds    skuId
     * @param shopIds   店铺
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    List<OrderHistorySalesEsEntity> findByShopIdInAndSkuIdInAndDateBetween(List<String> shopIds, List<String> skuIds, LocalDate startDate, LocalDate endDate);
    /**
     * 根据sku和店铺id查询一个范围内的销量
     *
     * @param skuIds    skuId
     * @param shopIds   店铺
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    Page<OrderHistorySalesEsEntity> findByShopIdInAndSkuIdInAndDateBetween(List<String> shopIds, List<String> skuIds, LocalDate startDate, LocalDate endDate, Pageable pageable);


    /**
     * 根据sku和店铺id查询一个范围内的销量
     *
     * @param skuId    skuId
     * @param shopId   店铺
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    List<OrderHistorySalesEsEntity> findByShopIdAndSkuIdAndDateBetween(String shopId, String skuId, LocalDate startDate, LocalDate endDate);
}
