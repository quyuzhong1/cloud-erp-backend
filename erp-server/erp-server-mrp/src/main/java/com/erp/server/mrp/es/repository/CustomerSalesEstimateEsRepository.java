package com.erp.server.mrp.es.repository;

import com.erp.server.mrp.es.entity.CustomerSalesEstimateEsEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSalesEstimateEsRepository extends ElasticsearchRepository<CustomerSalesEstimateEsEntity, String> {

    /**
     * 删除数据
     * @param platform 平台
     */
    void deleteByPlatform(String platform);
}
