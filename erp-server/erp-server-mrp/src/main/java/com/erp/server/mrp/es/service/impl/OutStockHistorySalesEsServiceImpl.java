package com.erp.server.mrp.es.service.impl;

import cn.hutool.json.JSONArray;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.entity.OutStockHistorySalesEsEntity;
import com.erp.server.mrp.es.repository.OutStockHistorySalesEsRepository;
import com.erp.server.mrp.es.service.OutStockHistorySalesEsService;
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
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.document.DocumentAdapters;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public Map<String, Integer> countQtyByShopSkuIdsAndDate(List<String> shopSkuId, JSONArray orderType, LocalDate startDate, LocalDate endDate) {
        List<OutStockHistorySalesEsEntity> outStockHistorySalesList = getOutStockHistorySales(shopSkuId, orderType, startDate, endDate);
        return outStockHistorySalesList.stream()
                .collect(Collectors.toMap(OutStockHistorySalesEsEntity::getShopSkuId, OutStockHistorySalesEsEntity::getOriginalSalesQty, Integer::sum));
    }

    /**
     * 查询历史数据
     *
     * @param shopSkuIds       shopId-skuId
     * @param orderType        销量类型
     * @param startDate        开始日期
     * @param endDate          结束日期
     */
    private List<OutStockHistorySalesEsEntity> getOutStockHistorySales(List<String> shopSkuIds, JSONArray orderType, LocalDate startDate, LocalDate endDate) {
        List<OutStockHistorySalesEsEntity> outStockHistorySalesList = new ArrayList<>();
        Page<OutStockHistorySalesEsEntity> outStockHistorySalesPage;
        int page = 0;
        do {
            if (CollectionUtils.isEmpty(orderType)) {
                outStockHistorySalesPage = outStockHistorySalesEsRepository.findByShopSkuIdInAndDateBetween(shopSkuIds, startDate, endDate, PageRequest.of(page, 10000));
            } else {
                outStockHistorySalesPage = outStockHistorySalesEsRepository.findByShopSkuIdInAndOrderTypeInAndDateBetween(shopSkuIds, orderType, startDate, endDate, PageRequest.of(page, 10000));
            }
            outStockHistorySalesList.addAll(outStockHistorySalesPage.toList());
            page++;
        } while (!outStockHistorySalesPage.isLast());
        return outStockHistorySalesList;
    }

    @Override
    public List<ReplenishmentResultDTO.SalesHistoryDTO> listByShopSkuIdsAndDate(List<String> suggestionIdList, JSONArray orderType, LocalDate startDate, LocalDate endDate) {
        List<OutStockHistorySalesEsEntity> historySales = getOutStockHistorySales(suggestionIdList, orderType, startDate, endDate);
        return historySales.stream()
                .map(v -> ReplenishmentResultDTO.SalesHistoryDTO.buildSalesHistory(v.getShopSkuId(), v.getDate(), v.getOriginalSalesQty()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByDateBetween(LocalDate startDate, LocalDate endDate) {
        outStockHistorySalesEsRepository.deleteByDateBetween(startDate, endDate);
    }

    @Override
    public Map<String, Set<String>> listSkuByShopId(Set<String> shopIds) {

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termsQuery("shopId", shopIds)))
                .withPageable(PageRequest.of(0, 10000))
                .build();
        List<String> scrollIdList = new ArrayList<>();
        List<OutStockHistorySalesEsEntity> result = new ArrayList<>();
        SearchScrollHits<OutStockHistorySalesEsEntity> orderHistorySales = elasticsearchRestTemplate.searchScrollStart(60000, searchQuery, OutStockHistorySalesEsEntity.class, IndexCoordinates.of("out_stock_history_sales"));
        String scrollId = orderHistorySales.getScrollId();
        scrollIdList.add(scrollId);
        while (true) {
            SearchScrollHits<OutStockHistorySalesEsEntity> searchScrollHits = elasticsearchRestTemplate.searchScrollContinue(scrollId, 60000, OutStockHistorySalesEsEntity.class, IndexCoordinates.of("out_stock_history_sales"));
            // 获取查询结果并收集到列表中
            List<OutStockHistorySalesEsEntity> products = searchScrollHits.getSearchHits().stream()
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
        return result.stream()
                .collect(Collectors.groupingBy(OutStockHistorySalesEsEntity::getShopId, Collectors.mapping(OutStockHistorySalesEsEntity::getSkuId, Collectors.toSet())));
    }
}
