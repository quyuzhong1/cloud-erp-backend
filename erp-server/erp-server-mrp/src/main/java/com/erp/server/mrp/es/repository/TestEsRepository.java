package com.erp.server.mrp.es.repository;

import com.erp.server.mrp.es.entity.TestEsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
 
/**
 * 在 Spring Data Elasticsearch 中，像 findByTitle 这样的方法命名并不是一个固定的约定，而是根据一定的命名规则自动生成查询方法。
 * Spring Data 提供了一种基于方法名的查询方法，可以根据方法名自动生成查询语句。
 * findByTitle(String title)：根据 title 字段查询记录。
deleteByAuthor(String author)：根据 author 字段删除记录。
countByCategoryAndPublishedDate(String category, Date publishedDate)：根据 category 和 publishedDate 字段统计记录数。
 * @author Administrator
 *
 */
@Repository
public interface TestEsRepository extends ElasticsearchRepository<TestEsEntity, String> {
	/**
	 * 通过code查询es数据
	 * @param code
	 * @return
	 */
	List<TestEsEntity> findByCode(String code);
	
	/**
	 * 通过code查询es分页数据
	 * @param code
	 * @param pageable
	 * @return
	 */
	Page<TestEsEntity> findByCode(String code , Pageable pageable);
	
	/**
	 * 通过code字段In和在date字段Between查询es分页数据
	 * @param codes
	 * @param startDate
	 * @param endDate
	 * @param pageable
	 * @return
	 */
	Page<TestEsEntity> findByCodeInAndDateBetween(List<String> codes, LocalDate startDate, LocalDate endDate, Pageable pageable);
}