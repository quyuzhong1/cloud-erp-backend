package com.common.business.feign;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface BaseDataFeign {
	@GetMapping("feign/baseData/queryValueByValue")
	List<Map<String, Object>> queryValueByValue(@RequestParam("tableName") String tableName, @RequestParam("queryFieldName") String queryFieldName,
            @RequestParam("queryValue") String queryValue, @RequestParam("returnFieldName") String returnFieldName, @RequestParam("extendQuerySql") String extendQuerySql);
}
