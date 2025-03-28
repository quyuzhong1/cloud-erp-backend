package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import com.common.core.utils.MathUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 订单详情字段映射转换
 */
@Service
@Scope("prototype")
public class TikTokFullySkuNextDmpHandler extends DmpInputDoNextDmpHandler {

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> detailList = (List<Map<String, Object>>) dmpInputMongoEntity.get("skus");
		detailList.forEach(d -> {
			d.put("spuCode", dmpInputMongoEntity.get("spuCode"));
		});
		return detailList;
	}


	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {

		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

				dmpDataMap.put("skuNo", dmpDataMap.get("externalSkuCode"));
				dmpDataMap.put("spuId", dmpDataMap.get("code"));
				//修改时间
				Object updateTimeObj = dmpDataMap.get("updateTime");
				if (updateTimeObj != null) {
					// 使用Instant类将Unix时间戳转换为LocalDateTime对象
					LocalDateTime updateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(updateTimeObj + "")), ZoneId.systemDefault());
					dmpDataMap.put("platformUpdateTime", updateTime);
				}
			}
		}
	}
}
