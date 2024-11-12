package com.erp.server.mrp.es.repository;

import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalcSalesInfoHisEsRepository  extends ElasticsearchRepository<CalcSalesInfoHisEsEntity, String> {
}
