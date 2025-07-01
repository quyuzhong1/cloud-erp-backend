package com.common.business.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.core.constant.EnumMessage;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;

@Data
public class DorisQuerySettingDTO {
	/**
	 * 数据源名称
	 */
	private String dataSourceName;
	/**
	 * 高级查询params属性名
	 */
	private String paramsField;
	/**
	 * 高级查询advanceQueryDTOList属性名
	 */
	private String advanceQueryDTOListField;
	/**
	 * 睡眠毫秒数
	 */
	private Integer sleepMillis;
	/**
	 * 高级查询条件配置
	 */
	private Map<String, List<String>> cfgField;
	
	public DynamicDataSourceTypeEnum getDynamicDataSourceType(String requestBody) {
		if(StringUtils.isBlank(dataSourceName) || DynamicDataSourceTypeEnum.POSTGRES.getCode().equals(dataSourceName)) {
			return DynamicDataSourceTypeEnum.POSTGRES;
		}
		if(StringUtils.isNotBlank(requestBody) && CollUtil.isNotEmpty(cfgField)) {
			JSONObject parseObject = JSON.parseObject(requestBody);
			if(parseObject == null) {
				return DynamicDataSourceTypeEnum.POSTGRES;
			}
			if(StringUtils.isBlank(paramsField)) {
				paramsField = "params";
			}
			JSONObject paramObject = parseObject.getJSONObject(paramsField);
			if(paramObject == null) {
				return DynamicDataSourceTypeEnum.POSTGRES;
			}
			
			if(StringUtils.isBlank(advanceQueryDTOListField)) {
				advanceQueryDTOListField = "advanceQueryDTOList";
			}
			JSONArray advanceQueryDTOListFieldList = paramObject.getJSONArray(advanceQueryDTOListField);
			if(CollUtil.isEmpty(advanceQueryDTOListFieldList)) {
				return DynamicDataSourceTypeEnum.POSTGRES;
			}
			
			List<AdvanceQueryDTO> advanceQueryDTOList = JSON.parseArray(advanceQueryDTOListFieldList.toJSONString(), AdvanceQueryDTO.class)
					.stream().filter(a -> {
						boolean valueFlag = false;
						Object value = a.getValue();
						if(value != null && StringUtils.isNotBlank(value.toString())) {
							valueFlag = true;
						}
						return StringUtils.isNotBlank(a.getField()) && StringUtils.isNotBlank(a.getCompare()) && valueFlag;
					}).collect(Collectors.toList());
			if(advanceQueryDTOList.stream().anyMatch(a -> {
				String field = a.getField();
				List<String> list = cfgField.get(field);
				return CollUtil.isNotEmpty(list) && (list.contains("all") || list.contains(a.getCompare()));
			})) {
				return DynamicDataSourceTypeEnum.POSTGRES;
			}
		}
		return EnumMessage.getByCode(DynamicDataSourceTypeEnum.class , dataSourceName);
	}
}
