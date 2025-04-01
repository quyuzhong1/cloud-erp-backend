package com.erp.server.mrp.es.service.impl;

import com.common.core.exception.ServiceException;
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
                .must(QueryBuilders.rangeQuery("date").gte(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))).lte(endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(PageRequest.of(0, 10000))
                .build();
        return getCustomerSales(searchQuery);
    }

    /**
     * 查询自定义销售数据
     * @param searchQuery
     * @return
     */
    private List<CustomerSalesEstimateEsEntity> getCustomerSales(NativeSearchQuery searchQuery) {
        List<String> scrollIdList = new ArrayList<>();
        List<CustomerSalesEstimateEsEntity> result = new ArrayList<>();
        try {
            // 1. 初始化滚动查询
            SearchScrollHits<CustomerSalesEstimateEsEntity> scrollHits = elasticsearchRestTemplate.searchScrollStart(
                    60_000, searchQuery, CustomerSalesEstimateEsEntity.class, IndexCoordinates.of("customer_sales_estimate"));
            String scrollId = scrollHits.getScrollId();
            scrollIdList.add(scrollId);

            // 2. 处理第一批数据
            processScrollHits(scrollHits, result);

            // 3. 循环处理后续批次
            while (true) {
                scrollHits = elasticsearchRestTemplate.searchScrollContinue(scrollId, 60_000,
                        CustomerSalesEstimateEsEntity.class, IndexCoordinates.of("customer_sales_estimate"));

                // 更新滚动ID并记录
                String newScrollId = scrollHits.getScrollId();
                scrollIdList.add(newScrollId);
                scrollId = newScrollId;

                // 处理数据并判断终止条件
                if (!processScrollHits(scrollHits, result)) {
                    break;
                }
            }
        } catch (Exception e) {
            // 异常处理（可添加日志或重试逻辑）
            throw new ServiceException("查询ES数据异常", e);
        } finally {
            // 4. 强制清理滚动上下文（确保资源释放）
            if (!scrollIdList.isEmpty()) {
                elasticsearchRestTemplate.searchScrollClear(scrollIdList);
            }
        }
        return result;
    }

    // 提取公共逻辑：处理结果集并返回是否继续
    private boolean processScrollHits(SearchScrollHits<CustomerSalesEstimateEsEntity> scrollHits,
                                      List<CustomerSalesEstimateEsEntity> result) {
        if (scrollHits == null || scrollHits.isEmpty()) {
            return false;
        }
        List<CustomerSalesEstimateEsEntity> batch = scrollHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        if (!batch.isEmpty()) {
            result.addAll(batch);
            return true;
        }
        return false;
    }
}
