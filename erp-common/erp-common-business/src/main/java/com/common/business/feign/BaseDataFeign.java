package com.common.business.feign;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignInvoke;

public interface BaseDataFeign {
	@GetMapping("feign/baseData/queryValueByValue")
	List<Map<String, Object>> queryValueByValue(@RequestParam("tableName") String tableName, @RequestParam("queryFieldName") String queryFieldName,
            @RequestParam("queryValue") String queryValue, @RequestParam("returnFieldName") String returnFieldName, @RequestParam("extendQuerySql") String extendQuerySql);
	
	@GetMapping("feign/baseData/queryValueByType")
	List<Map<String, Object>> queryValueByType(@RequestParam("tableName") String tableName, @RequestParam("queryFieldName") String queryFieldName,
			@RequestParam("returnFieldName") String returnFieldName, @RequestParam("queryTypeField") String queryTypeField);
	
	@PostMapping("feign/baseData/list")
	String list(@RequestBody FeignBuilder builder);
	
	@PostMapping("feign/baseData/invoke")
	String invoke(@RequestBody FeignInvoke feignInvoke);

	@GetMapping("feign/baseData/deleteArchiveData")
	int deleteArchiveData(@RequestParam("tableName") String tableName,
						  @RequestParam("timeField") String timeField,
						  @RequestParam("retentionDay") int retentionDay,
						  @RequestParam("limitCount") int limitCount,
						  @RequestParam("extSql") String extSql);
}
