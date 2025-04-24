package com.erp.server.mrp.controller.api;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.mrp.es.entity.TestEsEntity;
import com.erp.server.mrp.es.repository.OrderHistorySalesEsRepository;
import com.erp.server.mrp.es.repository.TestEsRepository;

@RestController
@RequestMapping("/testEs")
public class TestEsController extends BaseController {
	@Resource
	private TestEsRepository testEsRepository;
	@Resource
	private OrderHistorySalesEsRepository orderHistorySalesEsRepository;
	
	@Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
	
	/**
	 * 保存
	 * @param testEsEntity
	 * @return
	 */
	@PostMapping("/save")
    public ApiResult<?> save(@RequestBody TestEsEntity testEsEntity){
        return success(testEsRepository.save(testEsEntity));
    }
	
	/**
	 * 查询所有数据
	 * @return
	 */
	@PostMapping("/findAll")
	public ApiResult<?> findAll(){
		return success(testEsRepository.findAll(PageRequest.of(0, 3)).getContent());
	}
	
	/**
	 * 使用code查询
	 * @param testEsEntity
	 * @return
	 */
	@PostMapping("/findByCodeOrderByNameDesc")
	public ApiResult<?> findByCodeOrderByNameDesc(@RequestBody TestEsEntity testEsEntity){
		PageRequest pageable = PageRequest.of(0, 3);
		return success(testEsRepository.findByCodeOrderByNameDesc(testEsEntity.getCode() , pageable).getContent());
	}
	
	/**
	 * 使用code列表和时间范围查询
	 * @param testEsEntity
	 * @return
	 */
	@PostMapping("/findByCodeInAndDateBetween")
	public ApiResult<?> findByCodeInAndDateBetween(@RequestBody TestEsEntity testEsEntity){
		PageRequest pageable = PageRequest.of(0, 3);
		String code = testEsEntity.getCode();
		LocalDate date = testEsEntity.getDate();
		return success(testEsRepository.findByCodeInAndDateBetween(Arrays.asList(code),date, date, pageable));
	}
	
	/**
	 * 条件查询
	 * @param testEsEntity
	 * @return
	 */
	@PostMapping("/criteriaQuery")
	public ApiResult<?> criteriaQuery(@RequestBody TestEsEntity testEsEntity){
		Criteria criteria = new Criteria().and("code").is(testEsEntity.getCode())
				.and("name").in(testEsEntity.getName())
				.and("date").greaterThanEqual(testEsEntity.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
				.and("dateTime").lessThanEqual(testEsEntity.getDateTime().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'")));
		CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
		Sort sort = Sort.by(Sort.Order.asc("code"), Sort.Order.desc("dateTime"));
		criteriaQuery.setPageable(PageRequest.of(0, 3 , sort));
		SearchHits<TestEsEntity> search = elasticsearchRestTemplate.search(criteriaQuery, TestEsEntity.class);
		List<TestEsEntity> result = search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
		return success(result);
	}
	
	/**
	 * 条件删除
	 * @param testEsEntity
	 * @return
	 */
	@PostMapping("/deleteByQuery")
    public ApiResult<?> deleteByQuery(@RequestBody TestEsEntity testEsEntity){
		Criteria criteria = new Criteria().and("code").is(testEsEntity.getCode());
		CriteriaQuery query = new CriteriaQuery(criteria);
		elasticsearchRestTemplate.delete(query, TestEsEntity.class, elasticsearchRestTemplate.getIndexCoordinatesFor(TestEsEntity.class));
		return success();
    }
	
	/**
	 * 条件更新
	 * @param testEsEntity
	 * @return
	 */
	@PostMapping("/updateByQuery")
    public ApiResult<?> updateByQuery(@RequestBody TestEsEntity testEsEntity){
		String documentId = testEsEntity.getId();
        UpdateQuery updateQuery = UpdateQuery.builder(documentId)
                .withScript("ctx._source." + "name" + " = params.value")
                .withParams(Collections.singletonMap("value", testEsEntity.getName()))
                .build();
		UpdateResponse update = elasticsearchRestTemplate.update(updateQuery, elasticsearchRestTemplate.getIndexCoordinatesFor(TestEsEntity.class));
		return success(update);
    }
	
	/**
	 * 本地查询分组
	 * @param testEsEntity
	 * @return
	 */
	@PostMapping("/nativeSearchQuery")
	public ApiResult<?> nativeSearchQuery(@RequestBody TestEsEntity testEsEntity){
		NativeSearchQuery query = new NativeSearchQueryBuilder()
			    .withQuery(QueryBuilders.boolQuery()
			        .must(QueryBuilders.matchQuery("code", testEsEntity.getCode())) // matchQuery = eq
			        .must(QueryBuilders.termsQuery("name", testEsEntity.getName())) // termsQuery = in
			        .must(QueryBuilders.rangeQuery("date").gte(testEsEntity.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))))
			        .must(QueryBuilders.rangeQuery("dateTime").lte(testEsEntity.getDateTime().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'"))))
			     )
			    .addAggregation(AggregationBuilders.terms("by_code").field("code").subAggregation(AggregationBuilders.terms("by_name").field("name")))
			    .build();
		SearchHits<TestEsEntity> search = elasticsearchRestTemplate.search(query, TestEsEntity.class);
		Aggregations aggregations = search.getAggregations();
		Terms terms = aggregations.get("by_code");

		Map<String, Long> result = new HashMap<>();
		// 遍历聚合桶获取相关信息
		terms.getBuckets().forEach(bucket -> {
		    String key = bucket.getKeyAsString();
		    Aggregations aggregations2 = bucket.getAggregations();
		    Terms terms2 = aggregations2.get("by_name");
		    terms2.getBuckets().forEach(bucket2 -> {
		    	long docCount = bucket2.getDocCount();
		    	result.put(key + "_" + bucket2.getKeyAsString(), docCount);
		    });
		});
		return success(result);
	}
	
	/**
	 * 原生查询
	 * @param testEsEntity
	 * @return
	 */
	@PostMapping("/stringQuery")
	public ApiResult<?> stringQuery(@RequestBody TestEsEntity testEsEntity){
		String queryString = "{\r\n" + 
				"  \"query\": {\r\n" + 
				"    \"match_all\": {}\r\n" + 
				"  },\r\n" + 
				"  \"size\": 1\r\n" + 
				"}";
		StringQuery query = new StringQuery(queryString);
		SearchHits<TestEsEntity> search = elasticsearchRestTemplate.search(query, TestEsEntity.class);
		List<TestEsEntity> result = search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
		return success(result);
	}
}
