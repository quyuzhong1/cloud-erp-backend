package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderSoOutStockDmpHandler extends DmpInputAliExpressOrderDoChildDmpHandler{


	public static final String DELIVERY_TIME = "deliveryTime";
	public static final String SEND_FULFILL_TIME = "send_fulfill_time";


	@Override
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList){
		if(CollectionUtils.isNotEmpty(dmpInputMongoChildList)) {
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
				Object sendFulfillTimeObj = dmpInputMongoChild.get(SEND_FULFILL_TIME);
				if (null != sendFulfillTimeObj){
					LocalDateTime deliveryTime = LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) sendFulfillTimeObj), ZoneId.systemDefault());
					dmpInputMongoChild.put(DELIVERY_TIME, deliveryTime);
				}
			}
		}
		return dmpInputMongoChildList;
	}

	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
		DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
		String parentStorageName = mainConvertId.getStorageName();
		ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
		Map<String, String> billNoIdMap = new HashMap<>();
		if(CollUtil.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				billNoIdMap.put(listMap.get("third_code").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			String billNo = dmpInputMongoChildEntity.get("trade_order_no").toString();
			String dmpId = billNoIdMap.get(billNo);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
			dmpInputMongoChildEntity.put("sourceId", dmpId);
		}
	}
}
