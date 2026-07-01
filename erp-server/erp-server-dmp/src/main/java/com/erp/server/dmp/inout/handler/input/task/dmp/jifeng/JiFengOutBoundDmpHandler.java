package com.erp.server.dmp.inout.handler.input.task.dmp.jifeng;

import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
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
		    putDateTimeIfPresent(dmpDataMaps, "dateShipping", mongoData.get("shippedTime"));
			putDateTimeIfPresent(dmpDataMaps, "platformCreateTime", mongoData.get("createTime"));
		}
	}

	private void putDateTimeIfPresent(List<TreeMap<String, Object>> dmpDataMaps, String key, Object value) {
		if (Objects.isNull(value)) {
			return;
		}
		LocalDateTime dateTime = parseDateTime(String.valueOf(value));
		if (Objects.nonNull(dateTime)) {
			for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				dmpDataMap.put(key, dateTime);
			}
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
