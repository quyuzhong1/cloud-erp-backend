package com.erp.server.mrp.es.service;

import com.erp.server.mrp.es.entity.CustomerSalesEstimateEsEntity;

import java.util.List;

public interface CustomerSalesEstimateEsService {

    /**
     * 批量保存
     *
     * @param customerSalesEstimateList 用户预估日销量
     */
    void saveAll(List<CustomerSalesEstimateEsEntity> customerSalesEstimateList);
}
