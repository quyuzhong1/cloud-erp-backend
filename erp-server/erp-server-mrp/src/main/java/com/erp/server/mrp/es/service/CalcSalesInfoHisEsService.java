package com.erp.server.mrp.es.service;

import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;

import java.time.LocalDate;
import java.util.List;

public interface CalcSalesInfoHisEsService {

    /**
     * 批量保存
     * @param list 参数
     */
    void batchSave(List<CalcSalesInfoHisEsEntity> list);


    /**
     * 根据试算配置id，时间范围查询历史销量
     */
    List<CalcSalesInfoHisEsEntity> findByCfgRuleCalcIdAndSkuIdAndShopIdAndDateBetween(String cfgRuleCalcId, String skuId, String shopId,
                                                                                      LocalDate startDate, LocalDate endDate);

    /**
     * 根据试算配置id，查询历史销量
     */
    List<CalcSalesInfoHisEsEntity> findByCfgRuleCalcIdIn(List<String> cfgRuleCalcId);

    /**
     * 根据试算配置id，查询历史销量
     */
    List<CalcSalesInfoHisEsEntity> findByCfgRuleCalcIdAndShopIdAndSkuId(String cfgRuleCalcId, String shopId, String skuId);
}
