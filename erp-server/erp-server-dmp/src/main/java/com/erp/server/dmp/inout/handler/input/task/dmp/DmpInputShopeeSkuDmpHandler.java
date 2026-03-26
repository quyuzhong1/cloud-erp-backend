package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.oms.shopee.dto.PlatformShopeeListingDTO;
import com.sdk.oms.shopee.dto.product.response.Attribute;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputShopeeSkuDmpHandler extends DmpInputDoChildDmpHandler{
	
	@Override
	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList , String childMongoStorageName){
		List<Map<String, Object>> result = new ArrayList<>();
		List<ParamData> paramDataList = new ArrayList<>();
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).list();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
		List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, childMongoStorageName);
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
				Object model_list = dmpInputMongoChild.get("dmp_model_list");
				Boolean isModel = false;
				if(model_list != null) {
					List<Map<String , Object>> modelList = (List<Map<String , Object>>)model_list;
					if(CollUtil.isNotEmpty(modelList)) {
						isModel = true;
					}
				}


				dmpInputMongoChild.put("create_time", Long.valueOf(dmpInputMongoChild.get("create_time").toString()) * 1000L);
				dmpInputMongoChild.put("update_time", Long.valueOf(dmpInputMongoChild.get("update_time").toString()) * 1000L);
				Object dimensionObj = dmpInputMongoChild.get("dimension");
				if (dimensionObj != null) {
					Map<String, Object> dimension = (Map<String, Object>) dimensionObj;
					dmpInputMongoChild.put("packageLength", dimension.get("package_length"));
					dmpInputMongoChild.put("packageWidth", dimension.get("package_width"));
					dmpInputMongoChild.put("packageHeight", dimension.get("package_height"));
				}
				Object attribute_list_obj = dmpInputMongoChild.get("attribute_list");
				if (attribute_list_obj != null) {
					String attribute_list = JSON.toJSONString(attribute_list_obj);
					List<Attribute> attributeList = JSONUtil.toList(attribute_list, Attribute.class);
					String categoryName = PlatformShopeeListingDTO.processProductSpec(attributeList);
					dmpInputMongoChild.put("categoryName", categoryName);
				}
				Object imageObj = dmpInputMongoChild.get("image");
				if (imageObj != null) {
					Map<String, Object> image = (Map<String, Object>) imageObj;
					Object image_url_list_obj = image.get("image_url_list");
					if (image_url_list_obj != null) {
						List<Object> image_url_list = (List<Object>) image_url_list_obj;
						dmpInputMongoChild.put("imageUrls", image_url_list.stream().map(Object::toString).collect(Collectors.joining(";")));
					}
				}
				if(isModel){
					List<Map<String, Object>> modelList = (List<Map<String, Object>>) model_list;
					for (Map<String, Object> model : modelList) {
						Object skuIdObj = model.get("model_id");
						if (skuIdObj == null || StringUtils.isBlank(skuIdObj.toString())) {
							skuIdObj = dmpInputMongoChild.get("item_id");
						}
						dmpInputMongoChild.put("skuId", skuIdObj);
						Object skuNoObj = model.get("model_sku");
						if (skuNoObj == null || StringUtils.isBlank(skuNoObj.toString())) {
							skuNoObj = dmpInputMongoChild.get("item_sku");
						}
						dmpInputMongoChild.put("skuNo", skuNoObj);
						dmpInputMongoChild.put("name", model.get("model_name"));
						dmpInputMongoChild.put("status", model.get("model_status"));
						result.add(BeanUtil.copyProperties(dmpInputMongoChild, Map.class));
					}
				}else{
					dmpInputMongoChild.put("skuId", dmpInputMongoChild.get("item_id"));
					dmpInputMongoChild.put("skuNo", dmpInputMongoChild.get("item_sku"));
					dmpInputMongoChild.put("name", dmpInputMongoChild.get("item_name"));
					dmpInputMongoChild.put("status", dmpInputMongoChild.get("item_status"));
					result.add(BeanUtil.copyProperties(dmpInputMongoChild, Map.class));
				}

			}
		}
		return result;
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
				billNoIdMap.put(listMap.get("spu_id").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			Object spuId = dmpInputMongoChildEntity.get("item_id");
			if(spuId == null) {
				spuId = dmpInputMongoChildEntity.get("global_item_id");
			}
			String billNo = spuId.toString();
			String dmpId = billNoIdMap.get(billNo);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}
	
}
