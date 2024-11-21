package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpLogisticInfoService;
import com.erp.server.dmp.service.DmpSoInfoService;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderLogisticNextDmpHandler extends DmpInputDoNextDmpHandler{
	
	@Autowired
	private DmpSoInfoService dmpSoInfoService;
	
	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
		if(CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
			List<ParamData> paramDataList = new ArrayList<>();
			List<String> orderIdList = dmpInputMongoEntityList.stream().map(d -> d.get("order_id").toString()).collect(Collectors.toList());

			paramDataList.add(new ParamData("order_id", "order_id", PannoEnum.IN, orderIdList));
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
			List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_orderDetail_data");
			if(CollUtil.isNotEmpty(findMongoData)) {
				Map<String, Map<String, Object>> orderIdDetailMaps = findMongoData.stream().collect(Collectors.toMap(f -> f.get("order_id").toString(), f -> f));
				Map<String, String> maidIdThirdCodeMaps = dmpSoInfoService.lambdaQuery()
					.in(DmpSoInfoEntity::getThirdCode, orderIdDetailMaps.keySet())
					.eq(DmpSoInfoEntity::getInputTaskId, inputTaskId)
					.select(DmpSoInfoEntity::getId , DmpSoInfoEntity::getThirdCode)
					.list().stream().collect(Collectors.toMap(DmpSoInfoEntity::getThirdCode, DmpSoInfoEntity::getId));
				
				for(Map.Entry<String, Map<String, Object>> orderIdDetailMap : orderIdDetailMaps.entrySet()) {
					String ordreId = orderIdDetailMap.getKey();
					String mainId = maidIdThirdCodeMaps.get(ordreId);
					if(StringUtils.isNotBlank(mainId)) {
						Map<String, Object> orderDetails = orderIdDetailMap.getValue();
						Object logistic_info_list_obj = orderDetails.get("logistic_info_list");
						if(logistic_info_list_obj != null) {
							Object logistics_amount_obj = orderDetails.get("logistics_amount");
							String currencyCode = "";
							if(logistics_amount_obj != null) {
								Map<String, Object> logistics_amount = (Map)logistics_amount_obj;
								Object currencyCodeObj = logistics_amount.get("currency_code");
								if(currencyCodeObj != null) {
									currencyCode = currencyCodeObj.toString();
								}
							}
							String logisticsServiceName = "";
							Object child_order_list_obj = orderDetails.get("child_order_list");
							if(child_order_list_obj != null) {
								List<Map<String , Object>> child_order_list = (List<Map<String , Object>>)child_order_list_obj;
								if(CollUtil.isNotEmpty(child_order_list)) {
									Map<String, Object> child_order = child_order_list.get(0);
									Object logistics_service_name_obj = child_order.get("logistics_service_name");
									if(logistics_service_name_obj != null) {
										logisticsServiceName = logistics_service_name_obj.toString();
									}
								}
							}
							List<Map<String, Object>> logistic_info_list = (List<Map<String, Object>>)logistic_info_list_obj;
							ArrayList<TreeMap<String, Object>> valueList = new ArrayList<>();
							if(CollUtil.isNotEmpty(logistic_info_list)) {
								for(Map<String, Object> logistic_info : logistic_info_list) {
									TreeMap<String, Object> value = new TreeMap<>();
									value.put("mainId", mainId);
									value.put("logisticsNo", logistic_info.get("logistics_no"));
									value.put("deliveryTime", logistic_info.get("gmt_send"));
									value.put("logisticsServiceName", logisticsServiceName);
									value.put("logisticsTypeCode", logistic_info.get("logistics_type_code"));
									value.put("receiveStatus", logistic_info.get("receive_status"));
									value.put("currencyCode", currencyCode);
									value.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, nextLevelId);
									valueList.add(value);
								}
							}else {
								TreeMap<String, Object> value = new TreeMap<>();
								value.put("mainId", mainId);
								value.put("logisticsNo", "");
								value.put("logisticsServiceName", logisticsServiceName);
								value.put("currencyCode", currencyCode);
								value.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, nextLevelId);
								valueList.add(value);
							}
							dmpInputDataDmpRelationMaps.put(Collections.singletonList(orderDetails), valueList);
						}
					}
				}
			}
			
		}
		return dmpInputDataDmpRelationMaps;
	}
	
}
