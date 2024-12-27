package com.erp.server.mrp.es.service.impl;

import cn.hutool.json.JSONArray;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.repository.OrderHistorySalesEsRepository;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.google.common.collect.Lists;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.ParsedComposite;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.document.DocumentAdapters;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        return orderHistorySalesEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds, startDate, endDate, pageable);
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
    public Map<String, Integer> countQtyByReplenishmentIdsAndDate(List<String> replenishmentIds, JSONArray orderType, LocalDate startDate, LocalDate endDate) {
        List<OrderHistorySalesEsEntity> orderHistorySalesList = getOrderHistorySales(replenishmentIds, orderType, startDate, endDate);
        return orderHistorySalesList.stream()
                .collect(Collectors.toMap(OrderHistorySalesEsEntity::getReplenishmentId, OrderHistorySalesEsEntity::getOriginalSalesQty, Integer::sum));
    }


    /**
     * 查询历史数据
     *
     * @param replenishmentIds 建议id
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    private List<OrderHistorySalesEsEntity> getOrderHistorySales(List<String> replenishmentIds, JSONArray orderType, LocalDate startDate, LocalDate endDate) {
        List<OrderHistorySalesEsEntity> orderHistorySalesList = new ArrayList<>();
        Page<OrderHistorySalesEsEntity> orderHistorySalesPage;
        int page = 0;
        do {
            if (CollectionUtils.isEmpty(orderType)) {
                orderHistorySalesPage = orderHistorySalesEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds, startDate, endDate, PageRequest.of(page, 10000));
            } else {
                orderHistorySalesPage = orderHistorySalesEsRepository.findByReplenishmentIdInAndOrderTypeInAndDateBetween(replenishmentIds, orderType, startDate, endDate, PageRequest.of(page, 10000));
            }
            orderHistorySalesList.addAll(orderHistorySalesPage.toList());
            page++;
        } while (!orderHistorySalesPage.isLast());
        return orderHistorySalesList;
    }

    @Override
    public List<ReplenishmentResultDTO.SalesHistoryDTO> listByReplenishmentIdsAndDate(List<String> suggestionIdList, JSONArray orderType, LocalDate startDate, LocalDate endDate) {
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
                        .deleteByReplenishmentIdInAndDateBetween(suggestionList, startDate, endDate), threadPoolTaskExecutor))
                .toArray(CompletableFuture[]::new)).join();
    }

    @Override
    public List<OrderHistorySalesEsEntity> getRecentSalesBySuggestionIds(Set<String> suggestionIds, JSONArray orderType) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("replenishmentId", suggestionIds))
                .must(QueryBuilders.existsQuery("originalSalesQty"));
        if (!CollectionUtils.isEmpty(orderType)) {
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
    public Map<String, Integer> listByType(JSONArray orderType) {
        Map<String, Integer> result = new HashMap<>();
        int pageSize = 10000;
        Map<String, Object> afterKey = null;

        do {
            // 构建查询条件
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.existsQuery("originalSalesQty"));
            if (!CollectionUtils.isEmpty(orderType)) {
                queryBuilder.must(QueryBuilders.termsQuery("orderType", orderType));
            }

            // 构建 composite 聚合
            CompositeAggregationBuilder compositeAggregation = AggregationBuilders.composite("byReplenishmentId",
                            Collections.singletonList(new TermsValuesSourceBuilder("replenishmentId").field("replenishmentId")))
                    .size(pageSize)
                    .subAggregation(AggregationBuilders.topHits("latest_sales")
                            .sort(SortBuilders.fieldSort("date").order(SortOrder.DESC))
                            .size(1));

            // 设置分页参数
            if (afterKey != null) {
                compositeAggregation.aggregateAfter(afterKey);
            }

            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(queryBuilder)
                    .addAggregation(compositeAggregation)
                    .build();

            // 执行查询
            Aggregations aggregations = elasticsearchRestTemplate.search(searchQuery, OrderHistorySalesEsEntity.class)
                    .getAggregations();

            // 获取 composite 聚合结果
            ParsedComposite byReplenishmentId = aggregations.get("byReplenishmentId");
            for (ParsedComposite.ParsedBucket bucket : byReplenishmentId.getBuckets()) {
                TopHits topSales = bucket.getAggregations().get("latest_sales");
                org.elasticsearch.search.SearchHit searchHit = topSales.getHits().getAt(0);
                OrderHistorySalesEsEntity entity = elasticsearchRestTemplate.getElasticsearchConverter().read(OrderHistorySalesEsEntity.class, DocumentAdapters.from(searchHit));
                result.put(entity.getReplenishmentId(), entity.getOriginalSalesQty());
            }

            // 更新 afterKey，用于下一次查询
            afterKey = byReplenishmentId.afterKey();
        } while (afterKey != null);
        return result;
    }

    @Override
    public List<OrderHistorySalesEsEntity> findByShopIdInAndSkuIdInAndDateBetween(List<String> shopIds, List<String> skuIds, LocalDate startDate, LocalDate endDate) {
        List<OrderHistorySalesEsEntity> orderHistorySalesList = new ArrayList<>();
        Page<OrderHistorySalesEsEntity> orderHistorySalesPage;
        int page = 0;
        do {
            orderHistorySalesPage = orderHistorySalesEsRepository.findByShopIdInAndSkuIdInAndDateBetween(shopIds, skuIds, startDate, endDate, PageRequest.of(page, 10000));
            orderHistorySalesList.addAll(orderHistorySalesPage.toList());
            page++;
        } while (!orderHistorySalesPage.isLast());
        return orderHistorySalesList;
    }

    @Override
    public List<OrderHistorySalesEsEntity> findByShopIdInAndSkuIdInAndDateBetween(List<String> shopIds, List<String> skuIds, LocalDate startDate, LocalDate endDate, Object[] searchAfterValues) {

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termsQuery("skuId", skuIds))
                        .must(QueryBuilders.termsQuery("shopId", shopIds))
                        .must(QueryBuilders.rangeQuery("date").gte(startDate.format(DateTimeFormatter.BASIC_ISO_DATE)).lte(endDate.format(DateTimeFormatter.BASIC_ISO_DATE))))
                .withPageable(PageRequest.of(0, 10000))
                .build();
        List<String> scrollIdList = new ArrayList<>();
        List<OrderHistorySalesEsEntity> result = new ArrayList<>();
        SearchScrollHits<OrderHistorySalesEsEntity> orderHistorySales = elasticsearchRestTemplate.searchScrollStart(60000, searchQuery, OrderHistorySalesEsEntity.class, IndexCoordinates.of("order_history_sales"));
        String scrollId = orderHistorySales.getScrollId();
        scrollIdList.add(scrollId);
        while (true) {
            SearchScrollHits<OrderHistorySalesEsEntity> searchScrollHits = elasticsearchRestTemplate.searchScrollContinue(scrollId, 60000, OrderHistorySalesEsEntity.class, IndexCoordinates.of("order_history_sales"));
            // 获取查询结果并收集到列表中
            List<OrderHistorySalesEsEntity> products = searchScrollHits.getSearchHits().stream()
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
        // 返回结果
        return result;
    }

    @Override
    public List<OrderHistorySalesEsEntity> findByShopIdAndSkuIdAndDateBetween(String shopId, String skuId, LocalDate startDate, LocalDate endDate) {
        return orderHistorySalesEsRepository.findByShopIdAndSkuIdAndDateBetween(shopId, skuId, startDate, endDate);
    }

    @Override
    public List<String> hasSalesShopBySku(List<String> params) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("skuId", params));
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSourceFilter(new FetchSourceFilter(new String[]{"shopId"}, new String[]{}))
                .build();
        // 执行查询
        SearchHits<OrderHistorySalesEsEntity> searchHits = elasticsearchRestTemplate.search(searchQuery, OrderHistorySalesEsEntity.class);

        // 提取 shopId 并去重
        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getShopId())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> hasSalesSkuByShop(List<String> params) {

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("shopId", params));
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSourceFilter(new FetchSourceFilter(new String[]{"skuId"}, new String[]{}))
                .build();
        // 执行查询
        SearchHits<OrderHistorySalesEsEntity> searchHits = elasticsearchRestTemplate.search(searchQuery, OrderHistorySalesEsEntity.class);

        // 提取 shopId 并去重
        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getSkuId())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDateBetween(LocalDate startDate, LocalDate endDate) {
        orderHistorySalesEsRepository.deleteByDateBetween(startDate, endDate);
    }

    @Override
    public Map<String, List<String>> listSkuByShopId(Set<String> shopIds) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("shopId", shopIds));
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();
        // 执行查询
        SearchHits<OrderHistorySalesEsEntity> searchHits = elasticsearchRestTemplate.search(searchQuery, OrderHistorySalesEsEntity.class);
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.groupingBy(OrderHistorySalesEsEntity::getShopId, Collectors.mapping(OrderHistorySalesEsEntity::getSkuId, Collectors.toList())));
    }
}
