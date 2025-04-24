package com.erp.server.mrp.es.repository;

import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalcSalesInfoHisEsRepository extends ElasticsearchRepository<CalcSalesInfoHisEsEntity, String> {

    /**
     * 根据试算配置id，时间范围查询历史销量
     */
    List<CalcSalesInfoHisEsEntity> findByCfgRuleCalcIdAndSkuIdAndShopIdAndDateBetween(String cfgRuleCalcId, String skuId, String shopId,
                                                                                      LocalDate startDate, LocalDate endDate);

    /**
     * 根据试算配置id，历史销量
     */
    Page<CalcSalesInfoHisEsEntity> findByCfgRuleCalcIdIn(List<String> cfgRuleCalcId, Pageable pageable);


    /**
     * 根据试算配置id，查询历史销量
     */
    List<CalcSalesInfoHisEsEntity> findByCfgRuleCalcIdAndShopIdAndSkuId(String cfgRuleCalcId, String shopId, String skuId);

}
