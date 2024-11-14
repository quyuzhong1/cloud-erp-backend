package com.erp.server.mrp.es.service.impl;

import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;
import com.erp.server.mrp.es.repository.CalcSalesInfoHisEsRepository;
import com.erp.server.mrp.es.service.CalcSalesInfoHisEsService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CalcSalesInfoHisEsServiceImpl implements CalcSalesInfoHisEsService {

    @Resource
    private CalcSalesInfoHisEsRepository calcSalesInfoHisEsRepository;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void batchSave(List<CalcSalesInfoHisEsEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<List<CalcSalesInfoHisEsEntity>> partition = Lists.partition(list, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(dataList -> CompletableFuture.runAsync(() -> calcSalesInfoHisEsRepository.saveAll(dataList), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public List<CalcSalesInfoHisEsEntity> findByCfgRuleCalcIdAndSkuIdAndShopIdAndDateBetween(String cfgRuleCalcId, String skuId, String shopId, LocalDate startDate, LocalDate endDate) {
        return calcSalesInfoHisEsRepository.findByCfgRuleCalcIdAndSkuIdAndShopIdAndDateBetween(cfgRuleCalcId, skuId, shopId, startDate, endDate);
    }

}
