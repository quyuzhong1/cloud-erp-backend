package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Shopify配送明细
 */
@Slf4j
@Service
@Scope("prototype")
public class ShopifyOrderFulfillmentsDetailDmpHandler extends DmpInputDoNextDmpHandler{


	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> resultMapList = new ArrayList<>();
		long orderId = Long.parseLong(dmpInputMongoEntity.getOrDefault("orderId", "0").toString());
		if (orderId <= 0){
			return Collections.emptyList();
		}
		//订单配送信息
		List<ParamData> conditionDataList = new ArrayList<>();
		conditionDataList.add(new ParamData("order_id", "order_id", PannoEnum.EQ, orderId));
		List<Map<String, Object>> dmpInputFulfillmentMongoChildList = mongoService.findMongoData(conditionDataList, "shopify_fulfillments_data");
		if (CollectionUtils.isNotEmpty(dmpInputFulfillmentMongoChildList)){
			for (Map<String, Object> fulfillmentMainMongo : dmpInputFulfillmentMongoChildList) {
				// 补充主表信息字段
				Object itemsObj = fulfillmentMainMongo.get("line_items");
				List<Map<String, Object>> mapList = (List<Map<String, Object>>) itemsObj;
				// 补充主表信息字段
				String fulfillmentId = fulfillmentMainMongo.getOrDefault("id", "0").toString();
				for (Map<String, Object> item : mapList) {
					item.put("fulfillmentId", fulfillmentId);
				}
				// itemObj转Map<String, Object>
				resultMapList.addAll(mapList);
			}
		}
		return resultMapList;
	}

	@Override
	protected void afterConvertData(Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps) {
		log.debug("ShopifyOrderFulfillmentsDetailDmpHandler afterConvertData：");
		String parentTableName = SqlHelper.table(DmpSoOutstockEntity.class).getTableName();
		ServiceImpl parentServiceImpl = this.getServiceImpl(parentTableName);
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);

		Map<String, String> dmpFulfillmentIdIdMap = new HashMap<>();
		if(CollectionUtils.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				dmpFulfillmentIdIdMap.put(listMap.get("third_code").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for (List<TreeMap<String, Object>> dmpInputMongoList : dmpInputDataDmpRelationMaps.values()) {
			for (TreeMap<String, Object> detailMap : dmpInputMongoList) {
				String returnOrderId = detailMap.get("fulfillmentId").toString();
				String dmpId = dmpFulfillmentIdIdMap.get(returnOrderId);
				detailMap.put("mainId", dmpId);
			}
		}
		log.debug("ShopifyReturnOrderDetailDmpHandler afterConvertData：");
	}
}
