package com.erp.server.mrp.es.service.impl;

import com.erp.server.mrp.es.entity.CustomerSalesEstimateEsEntity;
import com.erp.server.mrp.es.repository.CustomerSalesEstimateEsRepository;
import com.erp.server.mrp.es.service.CustomerSalesEstimateEsService;
import com.google.common.collect.Lists;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    public List<CustomerSalesEstimateEsEntity> listByShopSkuIdAndDate(List<String> shopSkuIds, LocalDate startDate, LocalDate endDate) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("shopSkuId", shopSkuIds))
                .must(QueryBuilders.rangeQuery("date").gte(startDate.format(DateTimeFormatter.BASIC_ISO_DATE)).lte(endDate.format(DateTimeFormatter.BASIC_ISO_DATE)));
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(PageRequest.of(0, 10000))
                .build();
        return getCustomerSales(searchQuery);
    }


    private List<CustomerSalesEstimateEsEntity> getCustomerSales(NativeSearchQuery searchQuery) {
        List<String> scrollIdList = new ArrayList<>();
        List<CustomerSalesEstimateEsEntity> result = new ArrayList<>();
        SearchScrollHits<CustomerSalesEstimateEsEntity> customerSales = elasticsearchRestTemplate.searchScrollStart(60000, searchQuery, CustomerSalesEstimateEsEntity.class, IndexCoordinates.of("order_history_sales"));
        String scrollId = customerSales.getScrollId();
        scrollIdList.add(scrollId);
        if (!CollectionUtils.isEmpty(customerSales.getSearchHits())) {
            result.addAll(customerSales.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList()));
        }
        while (true) {
            SearchScrollHits<CustomerSalesEstimateEsEntity> searchScrollHits = elasticsearchRestTemplate.searchScrollContinue(scrollId, 60000, CustomerSalesEstimateEsEntity.class, IndexCoordinates.of("order_history_sales"));
            // 获取查询结果并收集到列表中
            List<CustomerSalesEstimateEsEntity> products = searchScrollHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            if (products.isEmpty()) {
                // 如果当前批次没有数据，表示所有数据已被检索完毕，退出循环
                break;
            }
            // 将当前批次的结果添加到全部结果列表中
            result.addAll(products);
            // 更新 scrollId 为当前批次的 scrollId
            scrollId = searchScrollHits.getScrollId();
        }
        elasticsearchRestTemplate.searchScrollClear(scrollIdList);
        return result;
    }
}
