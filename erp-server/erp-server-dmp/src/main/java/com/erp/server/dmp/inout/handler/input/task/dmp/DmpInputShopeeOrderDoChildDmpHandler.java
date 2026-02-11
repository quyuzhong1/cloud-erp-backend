package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.python.icu.math.BigDecimal;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp处理子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
@Slf4j
public class DmpInputShopeeOrderDoChildDmpHandler extends DmpInputDoChildDmpHandler{
	
	@Override
	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList , String childMongoStorageName){
		List<ParamData> paramDataList = new ArrayList<>();
		String childId = this.getDmpCfgInputChildId();
		if(StringUtils.isBlank(childId)) {
			throw new ServiceException("未查询到DmpInputShopeeOrderDoChildDmpHandler子类id");
		}
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).eq(DmpInputTaskEntity::getCfgInputId, "1801574477567165671").list();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
		List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "Shopee_orderDetail_data");
		return this.afterDoDmpInputMongoChildEntityList(dmpInputMongoChildList);
	}
	
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList){
		List<Map<String, Object>> resultList = new ArrayList<>();
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
				Object currencyObj = dmpInputMongoChild.get("currency");
				dmpInputMongoChild.put("currency_code", currencyObj);
				Object item_list = dmpInputMongoChild.get("item_list");
				if(item_list != null) {
					List<Map<String, Object>> itemList = (List<Map<String, Object>>)item_list;
					for(Map<String, Object> item : itemList) {
						Map copyProperties = BeanUtil.copyProperties(dmpInputMongoChild, Map.class);
						Object image_info = item.get("image_info");
						if(image_info != null) {
							Map<String, Object> imageInfo = (Map<String, Object>) image_info;
							copyProperties.put("skuUrl", imageInfo.get("image_url"));
						}
						String targetSku = item.getOrDefault("model_sku", "").toString();
						if (StringUtils.isBlank(targetSku)){
							targetSku = item.getOrDefault("item_sku", "").toString();
						}
						String skuName = item.getOrDefault("model_name", "").toString();
						if (StringUtils.isBlank(targetSku)){
							skuName = item.getOrDefault("item_name", "").toString();
						}
						copyProperties.put("skuName", skuName);
						String targetId = item.getOrDefault("model_id", "").toString();
						if (StringUtils.isBlank(targetId) || "0".equalsIgnoreCase(targetId)){
							targetId = item.getOrDefault("item_id", "").toString();
						}
						copyProperties.put("platformSku", targetSku);
						copyProperties.put("platformSpuNo", targetId);
						Object qtyObj = item.get("model_quantity_purchased");
						copyProperties.put("qty", qtyObj);
						Object priceObj = item.get("model_original_price");
						copyProperties.put("sellPriceOrigin", priceObj);
						
						Object sellPriceObj = item.get("model_discounted_price");
						copyProperties.put("sellPrice", sellPriceObj);
						if(qtyObj != null && sellPriceObj != null) {
							copyProperties.put("afterAmount", new BigDecimal(qtyObj.toString()).multiply(new BigDecimal(sellPriceObj.toString())));
						}
						copyProperties.put("thirdDetailId", item.get("model_id"));
						copyProperties.put("platformDetailId", item.get("order_item_id"));
						resultList.add(copyProperties);
					}
				}
			}
		}
		return resultList;
	}
	
	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList){
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
			String billNo = dmpInputMongoChildEntity.get("order_sn").toString();
			String dmpId = billNoIdMap.get(billNo);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}
}
