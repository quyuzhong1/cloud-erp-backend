package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoOutstockService;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderSoOutStockDetailDmpHandler extends DmpInputAliExpressOrderDoChildDmpHandler{
	
	@Resource
	private DmpSoOutstockService dmpSoOutstockService;
	
	@Override
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(
			List<Map<String, Object>> dmpInputMongoChildList) {
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			List<String> orderIdList = dmpInputMongoChildList.stream().map(d -> d.get("fulfillment_order_no").toString()).collect(Collectors.toList());
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData("fulfillment_order_no", "fulfillment_order_no", PannoEnum.IN, orderIdList));
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
			List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_soOutstock_data");
			if(CollUtil.isNotEmpty(findMongoData)) {
				Map<String, Map<String, Object>> orderNoMainMap = findMongoData.stream().collect(Collectors.toMap(f -> f.get("fulfillment_order_no").toString(), f -> f));
				for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
					Map<String, Object> mainMap = orderNoMainMap.get(dmpInputMongoChild.get("fulfillment_order_no"));
					dmpInputMongoChild.put("warehouseName", mainMap.get("warehouse_name"));
					dmpInputMongoChild.put("thirdOrderCode", mainMap.get("trade_order_no"));
					dmpInputMongoChild.put("platformOrderCode", mainMap.get("trade_order_no"));
				}
			}
		}
		return dmpInputMongoChildList;
	}
	
	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
		QueryWrapper<DmpSoOutstockEntity> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = dmpSoOutstockService.listMaps(wrapper);
		Map<String, String> billNoIdMap = new HashMap<>();
		if(CollUtil.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				billNoIdMap.put(listMap.get("third_code").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			String billNo = dmpInputMongoChildEntity.get("fulfillment_order_no").toString();
			String dmpId = billNoIdMap.get(billNo);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}
}
