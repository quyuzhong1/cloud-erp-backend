package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cItemStatusEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import org.apache.commons.collections4.CollectionUtils;
import org.python.icu.math.BigDecimal;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderDetailDmpHandler extends DmpInputAliExpressOrderDoChildDmpHandler{
	
	@Override
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList){
		List<Map<String, Object>> dmpInputMongoChildEntityList = new ArrayList<>();
		
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			List<Map<String, Object>> findMongoData = new ArrayList<>();
			List<ParamData> paramDataList = new ArrayList<>();
			List<String> orderIdList = dmpInputMongoChildList.stream().map(f -> f.get("order_id").toString()).collect(Collectors.toList());
			paramDataList.add(new ParamData("trade_order_no", "trade_order_no", PannoEnum.IN, orderIdList));
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.EQ, nextLevelId));
			findMongoData = mongoService.findMongoData(paramDataList, "aliexpress_soOutstock_data");
			Map<String, Map<String, Object>> soOutstockMaps = findMongoData.stream().collect(Collectors.toMap(f -> f.get("trade_order_no").toString(), f -> f , (f1 , f2) -> f2));
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
				AliExpressOrder sourceOrder = JSON.parseObject(JSON.toJSONString(dmpInputMongoChild), AliExpressOrder.class);
				Object child_order_list = dmpInputMongoChild.get("child_order_list");
				if(child_order_list != null) {
					List<Map<String, Object>> child_order_map_list = (List<Map<String, Object>>) child_order_list;
					child_order_map_list.forEach(c -> {
						Object order_id = dmpInputMongoChild.get("order_id");
						c.put("order_id", order_id);
						Map<String, Object> soOutstockMap = soOutstockMaps.get(order_id);
						if(soOutstockMap != null) {
							c.put("warehouseName", soOutstockMap.get("warehouse_name"));
						}
						Object product_count_obj = c.get("product_count");
						Object product_price_obj = c.get("product_price");
						if(product_count_obj != null && product_price_obj != null) {
							Map<String , Object> product_price = (Map)product_price_obj;
							Object amount_obj = product_price.get("amount");
							if(amount_obj != null) {
								c.put("sellPriceOrigin", new BigDecimal(product_count_obj.toString()).multiply(new BigDecimal(amount_obj.toString())));
							}
							Object currency_code_obj = product_price.get("currency_code");
							if(currency_code_obj != null) {
								c.put("currencyCode", currency_code_obj);
							}
						}

						Map<String, Object> lableMap = new HashMap<>();
						lableMap.put("alreadyTaxed", c.get("already_taxed"));
				        lableMap.put("logisticsWarehouseType", c.get("logistics_warehouse_type"));
				        lableMap.put("tagList", c.get("tags"));
						c.put("extendData", JSON.toJSONString(lableMap));
						
						c.put(DmpInputMongoHandler.MONGO_BASE_ID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_ID));
						c.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
					});
					dmpInputMongoChildEntityList.addAll(child_order_map_list);
				}
			}
		}
		
		return dmpInputMongoChildEntityList;
	}
	
}
