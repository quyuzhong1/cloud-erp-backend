package com.erp.server.mrp.es.repository;

import cn.hutool.json.JSONArray;
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
     * @param shopSkuIds shopId-skuId
     * @param startDate  开始时间
     * @param endDate    结束时间
     * @param pageable   分页参数
     */
    Page<OrderHistorySalesEsEntity> findByShopSkuIdInAndDateBetween(List<String> shopSkuIds, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 分页查询
     *
     * @param shopSkuIds shopId-skuId
     * @param orderType  订单类型
     * @param startDate  开始时间
     * @param endDate    结束时间
     * @param pageable   分页参数
     */
    Page<OrderHistorySalesEsEntity> findByShopSkuIdInAndOrderTypeInAndDateBetween(List<String> shopSkuIds, JSONArray orderType, LocalDate startDate, LocalDate endDate, Pageable pageable);


    /**
     * 根据sku和店铺id查询一个范围内的销量
     *
     * @param skuIds    skuId
     * @param shopIds   店铺
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param pageable  分页
     */
    Page<OrderHistorySalesEsEntity> findByShopIdInAndSkuIdInAndDateBetween(List<String> skuIds, List<String> shopIds, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 根据sku和店铺id查询一个范围内的销量
     *
     * @param skuId     skuId
     * @param shopId    店铺
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    List<OrderHistorySalesEsEntity> findByShopIdAndSkuIdAndDateBetween(String shopId, String skuId, LocalDate startDate, LocalDate endDate);

    void deleteByShopSkuIdInAndDateBetween(List<String> shopSkuIds, LocalDate startDate, LocalDate endDate);
    void deleteByDateBetween(LocalDate startDate, LocalDate endDate);
}
