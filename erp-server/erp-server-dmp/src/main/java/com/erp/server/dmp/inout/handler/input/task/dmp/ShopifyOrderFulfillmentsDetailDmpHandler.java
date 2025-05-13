package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.entity.BaseEntity;
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
		Map<String, Object> fulfillmentsMap = (Map<String, Object>) dmpInputMongoEntity;
		return  (List<Map<String, Object>>) fulfillmentsMap.get("line_items");
	}

	@Override
	protected void afterConvertData(Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps) {
		log.debug("ShopifyOrderFulfillmentsDetailDmpHandler afterConvertData：");
		String parentTableName = SqlHelper.table(DmpSoOutstockEntity.class).getTableName();
		ServiceImpl parentServiceImpl = this.getServiceImpl(parentTableName);
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);

		Map<String, String> dmpReturnIdMap = new HashMap<>();
		if(CollectionUtils.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				dmpReturnIdMap.put(listMap.get("third_code").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for (List<TreeMap<String, Object>> dmpInputMongoList : dmpInputDataDmpRelationMaps.values()) {
			for (TreeMap<String, Object> detailMap : dmpInputMongoList) {
				String returnOrderId = detailMap.get("third_bill_no").toString();
				String dmpId = dmpReturnIdMap.get(returnOrderId);
				detailMap.put("mainId", dmpId);
			}
		}
		log.debug("ShopifyReturnOrderDetailDmpHandler afterConvertData：");
	}
}
