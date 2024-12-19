package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.erp.server.dmp.utils.MapCountUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 订单详情字段映射转换
 */
@Service
@Scope("prototype")
public class MabangOrderNextDmpHandler extends MabangOrderGetDetailDmpHandler {

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
		if (CollUtil.isNotEmpty(detailList)) {
			detailList.forEach(d -> {
				d.put("platformOrderId", dmpInputMongoEntity.get("platformOrderId"));
			});
		} else {
			detailList = new ArrayList<>();
		}
		return detailList;
	}

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {

		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			HashMap<String, Integer> skuCountMap = new HashMap<>();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

				Object isGift = dmpDataMap.get("isGift");
				if (isGift != null) {
					//是否是赠品 1是 2否
					Integer returnedStatus = Integer.valueOf(isGift + "");
					if (returnedStatus == 1) {
						dmpDataMap.put("isGift", Boolean.TRUE);
					} else {
						dmpDataMap.put("isGift", Boolean.FALSE);
					}
				}

				Object skuNo = dmpDataMap.get("skuNo");
				Object platformOrderId = dmpDataMap.get("platformOrderId");
				if (skuNo != null) {
					//erp平台商品id
					String erpOrderItemId = platformOrderId + "_" + skuNo;
					erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, String.valueOf(skuNo), erpOrderItemId);
					dmpDataMap.put("thirdDetailId", erpOrderItemId);
				}
			}
		}
	}
}
