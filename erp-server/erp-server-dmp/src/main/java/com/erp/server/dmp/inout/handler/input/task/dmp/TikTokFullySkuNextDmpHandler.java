package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import com.common.core.utils.MathUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
			d.put("platformSpuCode", dmpInputMongoEntity.get("platformSpuCode"));
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

				//状态
				Object statusObj = dmpDataMap.get("status");
				if (statusObj != null) {
					String status = String.valueOf(statusObj);
					if ("UPSHELF".equalsIgnoreCase(status)) {
						dmpDataMap.put("status", "1");
					} else if ("DOWNSHELF".equalsIgnoreCase(status)) {
						dmpDataMap.put("status", "3");
					} else if ("WAIT_FOR_UPSHELF".equalsIgnoreCase(status)) {
						dmpDataMap.put("status", "5");
					}
				}
			}
		}
	}
}
