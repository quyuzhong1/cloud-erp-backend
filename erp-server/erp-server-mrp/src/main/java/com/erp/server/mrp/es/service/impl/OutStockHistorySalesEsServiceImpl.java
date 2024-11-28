package com.erp.server.mrp.es.service.impl;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.FbaOrderTypeEnum;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;
import com.erp.server.mrp.es.repository.OutStockHistorySalesEsRepository;
import com.erp.server.mrp.es.service.OutStockHistorySalesEsService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import com.google.common.collect.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.document.DocumentAdapters;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OutStockHistorySalesEsServiceImpl implements OutStockHistorySalesEsService {

    @Resource
    private OutStockHistorySalesEsRepository outStockHistorySalesEsRepository;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Page<OutStockHistorySalesEsEntity> findByReplenishmentIdInAndDateBetween(List<String> replenishmentIds, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return outStockHistorySalesEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds, startDate, endDate, pageable);
    }

    @Override
    public void saveAll(List<OutStockHistorySalesEsEntity> outStockHistorySales) {
        if (ObjectUtils.isEmpty(outStockHistorySales)) {
            return;
        }
        List<List<OutStockHistorySalesEsEntity>> partition = Lists.partition(outStockHistorySales, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(suggestionList -> CompletableFuture.runAsync(() -> outStockHistorySalesEsRepository.saveAll(suggestionList), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public Map<String, Integer> countQtyByReplenishmentIdsAndDate(List<String> replenishmentIds, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OutStockHistorySalesEsEntity> outStockHistorySalesList = getOutStockHistorySales(replenishmentIds, orderType, startDate, endDate);
        return outStockHistorySalesList.stream()
                .collect(Collectors.toMap(OutStockHistorySalesEsEntity::getReplenishmentId, OutStockHistorySalesEsEntity::getOriginalSalesQty, Integer::sum));
    }

    /**
     * 查询历史数据
     *
     * @param replenishmentIds 建议id
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    private List<OutStockHistorySalesEsEntity> getOutStockHistorySales(List<String> replenishmentIds, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OutStockHistorySalesEsEntity> outStockHistorySalesList = new ArrayList<>();
        Page<OutStockHistorySalesEsEntity> outStockHistorySalesPage;
        int page = 0;
        do {
            if (ObjectUtils.isEmpty(orderType) || FbaOrderTypeEnum.ALL.getCode().equals(orderType)) {
                outStockHistorySalesPage = outStockHistorySalesEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds, startDate, endDate, PageRequest.of(page, 10000));
            } else {
                outStockHistorySalesPage = outStockHistorySalesEsRepository.findByReplenishmentIdInAndOrderTypeAndDateBetween(replenishmentIds, orderType, startDate, endDate, PageRequest.of(page, 10000));
            }
            outStockHistorySalesList.addAll(outStockHistorySalesPage.toList());
            page++;
        } while (!outStockHistorySalesPage.isLast());
        return outStockHistorySalesList;
    }

    @Override
    public List<ReplenishmentResultDTO.SalesHistoryDTO> listByReplenishmentIdsAndDate(List<String> suggestionIdList, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OutStockHistorySalesEsEntity> historySales = getOutStockHistorySales(suggestionIdList, orderType, startDate, endDate);
        return historySales.stream()
                .map(v -> ReplenishmentResultDTO.SalesHistoryDTO.buildSalesHistory(v.getReplenishmentId(), v.getDate(), v.getOriginalSalesQty()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBySuggestionIdsAndDate(List<String> suggestionIds, LocalDate startDate, LocalDate endDate) {
        List<List<String>> partition = Lists.partition(suggestionIds, 1000);
        CompletableFuture.allOf(partition.stream()
                .map(suggestionList -> CompletableFuture.runAsync(() -> outStockHistorySalesEsRepository
                        .deleteByReplenishmentIdInAndDateBetween(suggestionList, startDate, endDate), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public List<OutStockHistorySalesEsEntity> getRecentSalesBySuggestionIds(Set<String> suggestionIds, String orderType) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.existsQuery("originalSalesQty"))
                .must(QueryBuilders.termsQuery("replenishmentId", suggestionIds));
        if (!ObjectUtils.isEmpty(orderType) && !FbaOrderTypeEnum.ALL.getCode().equals(orderType)) {
            queryBuilder.must(QueryBuilders.termsQuery("orderType", orderType));
        }
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .addAggregation(AggregationBuilders.terms("byReplenishmentId")
                        .field("replenishmentId")
                        .size(suggestionIds.size())
                        .subAggregation(AggregationBuilders.topHits("top_sales")
                                .sort(SortBuilders.fieldSort("date").order(SortOrder.DESC))
                                .size(1) // 只取最近的一条
                        )
                )
                .build();
        Aggregations aggregations = elasticsearchRestTemplate.search(searchQuery, OutStockHistorySalesEsEntity.class).getAggregations();
        Terms byReplenishmentId = aggregations.get("byReplenishmentId");
        return byReplenishmentId.getBuckets().stream()
                .map(bucket -> {
                    TopHits topSales = bucket.getAggregations().get("top_sales");
                    org.elasticsearch.search.SearchHit searchHit = topSales.getHits().getAt(0);
                    return elasticsearchRestTemplate.getElasticsearchConverter().read(OutStockHistorySalesEsEntity.class, DocumentAdapters.from(searchHit));
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> listByType(String orderType) {
        Map<String, Integer> result = new HashMap<>();
        int pageSize = 10000;
        for (int page = 0; ; page++) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.existsQuery("originalSalesQty"));
            if (!ObjectUtils.isEmpty(orderType) && !FbaOrderTypeEnum.ALL.getCode().equals(orderType)) {
                queryBuilder.must(QueryBuilders.termsQuery("orderType", orderType));
            }
            // 构建查询条件
            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(queryBuilder)
                    .addAggregation(AggregationBuilders.terms("byReplenishmentId")
                            .field("replenishmentId")
                            .size(pageSize)
                            .subAggregation(AggregationBuilders.topHits("latest_sales")
                                    .sort(SortBuilders.fieldSort("date").order(SortOrder.DESC))
                                    .size(1)
                            )
                    )
                    .withPageable(PageRequest.of(page, pageSize))
                    .build();
            // 执行查询
            Aggregations aggregations = elasticsearchRestTemplate.search(searchQuery, OutStockHistorySalesEsEntity.class)
                    .getAggregations();
            // 获取 replenishmentId 的聚合结果
            Terms byReplenishmentId = aggregations.get("byReplenishmentId");
            for (Terms.Bucket bucket : byReplenishmentId.getBuckets()) {
                TopHits topSales = bucket.getAggregations().get("latest_sales");
                org.elasticsearch.search.SearchHit searchHit = topSales.getHits().getAt(0);
                OutStockHistorySalesEsEntity entity = elasticsearchRestTemplate.getElasticsearchConverter().read(OutStockHistorySalesEsEntity.class, DocumentAdapters.from(searchHit));
                result.put(entity.getReplenishmentId(), entity.getOriginalSalesQty());
            }

            // 判断是否还有更多数据
            if (byReplenishmentId.getBuckets().size() < pageSize) {
                break; // 如果当前页的桶数少于 pageSize，说明没有更多数据
            }
        }
        return result;
    }
}
