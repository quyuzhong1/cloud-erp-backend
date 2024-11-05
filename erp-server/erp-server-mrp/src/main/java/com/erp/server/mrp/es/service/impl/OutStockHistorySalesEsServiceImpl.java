package com.erp.server.mrp.es.service.impl;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.FbaOrderTypeEnum;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
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
import org.springframework.data.elasticsearch.core.SearchHit;
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
                        .deleteByReplenishmentIdInAndAndDateBetween(suggestionList, startDate, endDate), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public List<OutStockHistorySalesEsEntity> getRecentSalesBySuggestionIds(Set<String> suggestionIds, String orderType) {

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
            List<OutStockHistorySalesEsEntity> entities = elasticsearchRestTemplate.search(searchQuery, OutStockHistorySalesEsEntity.class)
                    .stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
            // 将结果映射到 result Map 中
            result.putAll(entities.stream()
                    .collect(Collectors.toMap(OutStockHistorySalesEsEntity::getId, OutStockHistorySalesEsEntity::getOriginalSalesQty, (o1, o2) -> o1)));
            // 判断是否还有更多数据，如果没有则退出循环
            if (entities.size() < pageSize) {
                break;
            }
            page++;
        }
        return result;
    }
}
