package com.erp.server.mrp.es.service;

import com.erp.server.mrp.es.entity.CustomerSalesEstimateEsEntity;

import java.time.LocalDate;
import java.util.List;

public interface CustomerSalesEstimateEsService {

    /**
     * 批量保存
     *
     * @param customerSalesEstimateList 用户预估日销量
     */
    void saveAll(List<CustomerSalesEstimateEsEntity> customerSalesEstimateList);

    /**
     * 移除数据
     * @param platform 平台
     */
    void removeByPlatform(String platform);

    /**
     * 查询数据
     * @param shopSkuId 店铺sku id
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    List<CustomerSalesEstimateEsEntity> listByShopSkuIdAndDate(List<String> shopSkuId, LocalDate startDate, LocalDate endDate);
}
