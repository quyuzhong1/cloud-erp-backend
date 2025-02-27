package com.erp.server.mrp.es.service.impl;

import com.erp.server.mrp.es.entity.CustomerSalesEstimateEsEntity;
import com.erp.server.mrp.es.repository.CustomerSalesEstimateEsRepository;
import com.erp.server.mrp.es.service.CustomerSalesEstimateEsService;
import com.google.common.collect.Lists;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CustomerSalesEstimateEsServiceImpl implements CustomerSalesEstimateEsService {

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private CustomerSalesEstimateEsRepository customerSalesEstimateEsRepository;


    @Override
    public void saveAll(List<CustomerSalesEstimateEsEntity> customerSalesEstimateList) {
        if (CollectionUtils.isEmpty(customerSalesEstimateList)) {
            return;
        }
        List<List<CustomerSalesEstimateEsEntity>> partition = Lists.partition(customerSalesEstimateList, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(estimateList -> CompletableFuture.runAsync(() -> customerSalesEstimateEsRepository.saveAll(estimateList), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public void removeByPlatform(String platform) {
        customerSalesEstimateEsRepository.deleteByPlatform(platform);
    }

    @Override
    public List<CustomerSalesEstimateEsEntity> listByShopSkuIdAndDate(List<String> shopSkuId, LocalDate startDate, LocalDate endDate) {
        return null;
    }
}
