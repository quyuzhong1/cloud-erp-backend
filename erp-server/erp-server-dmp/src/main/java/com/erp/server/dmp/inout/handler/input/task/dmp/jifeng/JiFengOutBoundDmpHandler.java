package com.erp.server.dmp.inout.handler.input.task.dmp.jifeng;

import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class JiFengOutBoundDmpHandler extends DmpInputDbConvertDmpHandler {

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
			Map<String, Object> mongoData = mongoDataMaps.get(0);
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				dmpDataMap.put("referenceNo", mongoData.get("erpNo"));
			}
		    putDateTimeIfPresent(dmpDataMaps, "dateShipping", mongoData.get("shippedTime"), mongoData.get("erpNo"));
			putDateTimeIfPresent(dmpDataMaps, "platformCreateTime", mongoData.get("createTime"), mongoData.get("erpNo"));
		}
	}

	private void putDateTimeIfPresent(List<TreeMap<String, Object>> dmpDataMaps, String key, Object value, Object referenceNo) {
		if (Objects.isNull(value)) {
			return;
		}
		String dateTimeStr = String.valueOf(value);
		LocalDateTime dateTime = parseDateTime(dateTimeStr);
		if (Objects.isNull(dateTime)) {
			log.error("JiFeng出库时间解析失败，referenceNo={}，字段={}，原始值={}", referenceNo, key, dateTimeStr);
			throw new ServiceException(String.format("JiFeng出库时间解析失败，referenceNo=%s，字段=%s，原始值=%s",
					referenceNo, key, dateTimeStr));
		}
		for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
			dmpDataMap.put(key, dateTime);
		}
	}

	private LocalDateTime parseDateTime(String dateTimeStr) {
		if (StringUtils.isBlank(dateTimeStr)) {
			return null;
		}
		try {
			return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		} catch (Exception ignored) {
		}
		try {
			return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
		} catch (Exception ignored) {
		}
		try {
			return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		} catch (Exception ignored) {
		}
		return null;
	}
}
