package com.erp.server.mrp.es.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.FbaOrderTypeEnum;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;
import com.erp.server.mrp.es.repository.OrderHistorySalesEsRepository;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.google.common.collect.Lists;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.document.DocumentAdapters;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OrderHistorySalesEsServiceImpl implements OrderHistorySalesEsService {

    @Resource
    private OrderHistorySalesEsRepository orderHistorySalesEsRepository;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    @Override
    public Page<OrderHistorySalesEsEntity> findByReplenishmentIdInAndDateBetween(List<String> replenishmentIds, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return orderHistorySalesEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds,startDate,endDate,pageable);
    }

    @Override
    public void saveAll(List<OrderHistorySalesEsEntity> orderHistorySales) {
        if (CollectionUtils.isEmpty(orderHistorySales)) {
            return;
        }
        List<List<OrderHistorySalesEsEntity>> partition = Lists.partition(orderHistorySales, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(suggestionList -> CompletableFuture.runAsync(() -> orderHistorySalesEsRepository.saveAll(suggestionList), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public Map<String, Integer> countQtyByReplenishmentIdsAndDate(List<String> replenishmentIds, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OrderHistorySalesEsEntity> orderHistorySalesList = getOrderHistorySales(replenishmentIds, orderType, startDate, endDate);
        return orderHistorySalesList.stream()
                .collect(Collectors.toMap(OrderHistorySalesEsEntity::getReplenishmentId, OrderHistorySalesEsEntity::getOriginalSalesQty, Integer::sum));
    }


    /**
     * 查询历史数据
     * @param replenishmentIds 建议id
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    private List<OrderHistorySalesEsEntity> getOrderHistorySales(List<String> replenishmentIds, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OrderHistorySalesEsEntity> orderHistorySalesList = new ArrayList<>();
        Page<OrderHistorySalesEsEntity> orderHistorySalesPage;
        int page = 0;
        do {
            if (ObjectUtils.isEmpty(orderType) || FbaOrderTypeEnum.ALL.getCode().equals(orderType)) {
                orderHistorySalesPage = orderHistorySalesEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds, startDate, endDate, PageRequest.of(page, 10000));
            } else {
                orderHistorySalesPage = orderHistorySalesEsRepository.findByReplenishmentIdInAndOrderTypeAndDateBetween(replenishmentIds, orderType, startDate, endDate, PageRequest.of(page, 10000));
            }
            orderHistorySalesList.addAll(orderHistorySalesPage.toList());
            page++;
        } while (!orderHistorySalesPage.isLast());
        return orderHistorySalesList;
    }

    @Override
    public List<ReplenishmentResultDTO.SalesHistoryDTO> listByReplenishmentIdsAndDate(List<String> suggestionIdList, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OrderHistorySalesEsEntity> historySales = getOrderHistorySales(suggestionIdList, orderType, startDate, endDate);
        return historySales.stream()
                .map(v -> ReplenishmentResultDTO.SalesHistoryDTO.buildSalesHistory(v.getReplenishmentId(), v.getDate(), v.getOriginalSalesQty()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBySuggestionIdsAndDate(List<String> suggestionIds, LocalDate startDate, LocalDate endDate) {
        List<List<String>> partition = Lists.partition(suggestionIds, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(suggestionList -> CompletableFuture.runAsync(() -> orderHistorySalesEsRepository
                        .deleteByReplenishmentIdInAndAndDateBetween(suggestionList, startDate, endDate), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public List<OrderHistorySalesEsEntity> getRecentSalesBySuggestionIds(Set<String> suggestionIds, String orderType) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termsQuery("replenishmentId", suggestionIds))
                        .must(QueryBuilders.existsQuery("originalSalesQty"))
                )
                .addAggregation(AggregationBuilders.terms("byReplenishmentId")
                        .field("replenishmentId")
                        .size(suggestionIds.size())
                        .subAggregation(AggregationBuilders.topHits("top_sales")
                                .sort(SortBuilders.fieldSort("date").order(SortOrder.DESC))
                                .size(1) // 只取最近的一条
                        )
                )
                .build();
        Aggregations aggregations = elasticsearchRestTemplate.search(searchQuery, OrderHistorySalesEsEntity.class).getAggregations();
        Terms byReplenishmentId = aggregations.get("byReplenishmentId");
        return byReplenishmentId.getBuckets().stream()
                .map(bucket -> {
                    TopHits topSales = bucket.getAggregations().get("top_sales");
                    org.elasticsearch.search.SearchHit searchHit = topSales.getHits().getAt(0);
                    return elasticsearchRestTemplate.getElasticsearchConverter().read(OrderHistorySalesEsEntity.class, DocumentAdapters.from(searchHit));
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> listByType(String orderType) {
        Map<String, Integer> result = new HashMap<>();
        int page = 0;
        int pageSize = 10000;
        while (true) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termsQuery("orderType", orderType))
                    .must(QueryBuilders.existsQuery("originalSalesQty"));
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(queryBuilder)
                    .withPageable(PageRequest.of(page, pageSize))
                    .withSort(SortBuilders.fieldSort("date").order(SortOrder.DESC))
                    .build();
            List<OrderHistorySalesEsEntity> entities = elasticsearchRestTemplate.search(searchQuery, OrderHistorySalesEsEntity.class)
                    .stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
            // 将结果映射到 result Map 中
            result.putAll(entities.stream()
                    .collect(Collectors.toMap(OrderHistorySalesEsEntity::getId, OrderHistorySalesEsEntity::getOriginalSalesQty, (o1, o2) -> o1)));
            // 判断是否还有更多数据，如果没有则退出循环
            if (entities.size() < pageSize) {
                break;
            }
            page++;
        }
        return result;
    }
}
