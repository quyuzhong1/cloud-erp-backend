package com.erp.server.mrp.es.service;

import cn.hutool.json.JSONArray;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OrderHistorySalesEsService {

    /**
     * 批量保存
     *
     * @param orderHistorySales 历史库存
     */
    void saveAll(List<OrderHistorySalesEsEntity> orderHistorySales);

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
    List<OrderHistorySalesEsEntity> findByShopIdInAndSkuIdInAndDateBetween(List<String> shopIds, List<String> skuIds, LocalDate startDate, LocalDate endDate, Object[] searchAfterValues);


    /**
     * 根据sku和店铺id查询一个范围内的销量
     *
     * @param skuId    skuId
     * @param shopId   店铺
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    List<OrderHistorySalesEsEntity> findByShopIdAndSkuIdAndDateBetween(String shopId, String skuId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据sku查询有销量店铺
     * @param params 参数
     */
    List<String> hasSalesShopBySku(List<String> params);

    /**
     * 根据店铺查询有销量sku
     * @param params 参数
     */
    List<String> hasSalesSkuByShop(List<String> params);

    /**
     * 删除历史数据
     *
     * @param shopSkuIds  店铺skuId
     * @param startDate  开始日期
     * @param endDate    结束日期
     */
    void deleteByDateBetween(List<String> shopSkuIds, LocalDate startDate, LocalDate endDate);

    /**
     * 删除历史数据
     *
     * @param startDate  开始日期
     * @param endDate    结束日期
     */
    void deleteByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根据店铺查询有销量sku
     * @param shopIds 店铺
     */
    Map<String, Set<String>> listSkuByShopId(Set<String> shopIds);
}
