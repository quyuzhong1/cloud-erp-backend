package com.erp.server.mrp.es.service.impl;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.enums.FbaOrderTypeEnum;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.repository.OrderHistorySalesEsRepository;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderHistorySalesEsServiceImpl implements OrderHistorySalesEsService {

    @Resource
    private OrderHistorySalesEsRepository orderHistorySalesEsRepository;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    @Override
    public Page<OrderHistorySalesEsEntity> findByReplenishmentIdInAndDateBetween(List<String> replenishmentIds, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return orderHistorySalesEsRepository.findByReplenishmentIdInAndDateBetween(replenishmentIds,startDate,endDate,pageable);
    }

    @Override
    public void saveAll(List<OrderHistorySalesEsEntity> historyInventoryList) {
        if (CollectionUtils.isEmpty(historyInventoryList)) {
            return;
        }
        orderHistorySalesEsRepository.saveAll(historyInventoryList);
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
    public void deleteByIdIn(List<String> deleteIds) {
        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        orderHistorySalesEsRepository.deleteByIdIn(deleteIds);
    }

    @Override
    public List<ReplenishmentResultDTO.SalesHistoryDTO> listByReplenishmentIdsAndDate(List<String> suggestionIdList, String orderType, LocalDate startDate, LocalDate endDate) {
        List<OrderHistorySalesEsEntity> historySales = getOrderHistorySales(suggestionIdList, orderType, startDate, endDate);
        return historySales.stream()
                .map(v -> ReplenishmentResultDTO.SalesHistoryDTO.buildSalesHistory(v.getReplenishmentId(), v.getDate(), v.getOriginalSalesQty()))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderHistorySalesEsEntity> getRecentSalesBySuggestionIds(Set<String> suggestionIds, String orderType) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("replenishmentId", suggestionIds))
                .must(QueryBuilders.existsQuery("originalSalesQty"));
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(PageRequest.of(0, 10000))
                .withSort(SortBuilders.fieldSort("date").order(SortOrder.DESC))
                .build();
        return elasticsearchRestTemplate.search(searchQuery, OrderHistorySalesEsEntity.class)
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
