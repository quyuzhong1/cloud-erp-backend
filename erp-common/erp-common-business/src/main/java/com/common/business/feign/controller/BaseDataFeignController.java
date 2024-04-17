package com.common.business.feign.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.feign.BaseDataFeign;
import com.common.business.mapper.BaseDataMapper;

@RestController
@RequestMapping("feign/baseData")
public class BaseDataFeignController implements BaseDataFeign{

	@Autowired(required = false)
	private BaseDataMapper baseDataMapper;
	
	@GetMapping("/queryValueByValue")
	@Override
	public List<Map<String, Object>> queryValueByValue(String tableName, String queryFieldName, String queryValue, String returnFieldName , String extendQuerySql) {
		return baseDataMapper.queryValueByValue(tableName, queryFieldName, queryValue, returnFieldName , extendQuerySql);
	}

	@GetMapping("/queryValueByType")
	@Override
	public List<Map<String, Object>> queryValueByType(String tableName, String queryFieldName,
			String returnFieldName, String queryTypeField) {
		return baseDataMapper.queryValueByType(tableName, queryFieldName, returnFieldName , queryTypeField);
	}

}
