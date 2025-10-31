package com.erp.server.dmp.inout.handler.input.task.dmp.weishi;

import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class WeiShiReturnInstockDmpHandler extends DmpInputDbConvertDmpHandler {

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

				String createTimeStr = dmpDataMap.getOrDefault("createTime", "").toString();
				if (StringUtils.isNotBlank(createTimeStr)){
					LocalDateTime createTime = LocalDateTime.parse(createTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
					createTime = createTime.plusHours(11);
					dmpDataMap.put("platformCreateTime", createTime);
				}

				String finishTimeStr = dmpDataMap.getOrDefault("receiveTime", "").toString();
				if (StringUtils.isNotBlank(finishTimeStr)){
					LocalDateTime finishTime = LocalDateTime.parse(finishTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
					finishTime = finishTime.plusHours(11);
					dmpDataMap.put("putAwayTime", finishTime);
				}
			}
		}
	}
}
