package com.erp.server.dmp.inout.handler.input.task.dmp.weishi;

import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
public class WeiShiOutBoundDmpHandler extends DmpInputDbConvertDmpHandler {

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
			Map<String, Object> mongoData = mongoDataMaps.get(0);
			if(mongoData.containsKey("checkOutDate") && Objects.nonNull(mongoData.get("checkOutDate"))){
				String shippedTimeStr = String.valueOf(mongoData.get("checkOutDate"));
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				// 解析为 LocalDateTime 对象
				LocalDateTime shippedTime = LocalDateTime.parse(shippedTimeStr, formatter);
				String warehouseCode = String.valueOf(mongoData.get("warehouseCode"));
				if("MXW1".equals(warehouseCode)){
					// 1. 解析为北京时间（无时区）
					// 2. 关联北京时区
					ZonedDateTime beijingZoned = shippedTime.atZone(ZoneId.of("Asia/Shanghai"));
					// 3. 转换到墨西哥六区（固定 UTC-6）
					ZonedDateTime mexicoZoned = beijingZoned.withZoneSameInstant(ZoneOffset.ofHours(-6));
					// 4. 提取转换后的本地时间
					shippedTime = mexicoZoned.toLocalDateTime();
				}
				for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
					dmpDataMap.put("dateShipping", shippedTime);
				}
			}
		}
	}

}
