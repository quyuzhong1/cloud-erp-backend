package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.init.api.AliExpressOverseasManagedProductHelper;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;

/**
 * 速卖通海外托管商品详情展开为SKU数据。
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOverseasManagedSkuDmpHandler extends DmpInputDoChildDmpHandler {

	@Override
	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList,
			String childMongoStorageName) {
		List<Map<String, Object>> result = new ArrayList<>();
		List<DmpInputTaskEntity> childTasks = dmpInputTaskService.lambdaQuery()
				.eq(DmpInputTaskEntity::getParentTaskId, inputTaskId)
				.list();
		if (CollUtil.isEmpty(childTasks)) {
			return result;
		}
		List<String> childTaskIds = new ArrayList<>();
		for (DmpInputTaskEntity childTask : childTasks) {
			childTaskIds.add(childTask.getId());
		}
		List<ParamData> paramDataList = new ArrayList<>();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID,
				DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.IN, childTaskIds));
		List<Map<String, Object>> childMongoList = mongoService.findMongoData(paramDataList, childMongoStorageName);
		if (CollUtil.isEmpty(childMongoList)) {
			return result;
		}
		for (Map<String, Object> childMongo : childMongoList) {
			result.addAll(expandSku(childMongo));
		}
		return result;
	}

	private List<Map<String, Object>> expandSku(Map<String, Object> childMongo) {
		List<Map<String, Object>> result = new ArrayList<>();
		JSONObject detail = JSONObject.parseObject(JSON.toJSONString(childMongo));
		JSONObject productInfo = AliExpressOverseasManagedProductHelper.findObject(detail, "product_info_dto", "productInfoDto");
		if (productInfo == null) {
			productInfo = detail;
		}
		String spuId = firstNotBlank(
				AliExpressOverseasManagedProductHelper.findString(productInfo, "product_id", "productId"),
				AliExpressOverseasManagedProductHelper.findString(detail, "parentProductId"));
		JSONArray skuList = AliExpressOverseasManagedProductHelper.findArray(detail, "product_sku_list", "productSkuList");
		if (skuList.isEmpty()) {
			return result;
		}
		for (Object item : skuList) {
			if (!(item instanceof JSONObject)) {
				continue;
			}
			JSONObject sku = (JSONObject) item;
			Map<String, Object> flatSku = new HashMap<>();
			flatSku.put("platformCreateTime", firstNotBlank(
					AliExpressOverseasManagedProductHelper.findString(productInfo, "gmt_create", "gmtCreate"),
					AliExpressOverseasManagedProductHelper.findString(detail, "gmt_create", "gmtCreate")));
			flatSku.put("platformUpdateTime", firstNotBlank(
					AliExpressOverseasManagedProductHelper.findString(productInfo, "gmt_modified", "gmtModified"),
					AliExpressOverseasManagedProductHelper.findString(detail, "gmt_modified", "gmtModified")));
			flatSku.put("spuId", spuId);
			flatSku.put("skuId", AliExpressOverseasManagedProductHelper.findString(sku, "sku_id", "skuId"));
			flatSku.put("skuNo", AliExpressOverseasManagedProductHelper.findString(sku, "sku_code", "skuCode"));
			flatSku.put("status", AliExpressOverseasManagedProductHelper.mapStatus(
					AliExpressOverseasManagedProductHelper.findString(sku, "status")));
			flatSku.put("name", AliExpressOverseasManagedProductHelper.joinSkuProperties(sku));
			flatSku.put("imageUrls", AliExpressOverseasManagedProductHelper.joinImages(detail, sku));
			putIfNotBlank(flatSku, "packageLength", firstNotBlank(
					AliExpressOverseasManagedProductHelper.findString(sku, "package_length", "packageLength", "length"),
					AliExpressOverseasManagedProductHelper.findString(detail, "package_length", "packageLength", "length")));
			putIfNotBlank(flatSku, "packageWidth", firstNotBlank(
					AliExpressOverseasManagedProductHelper.findString(sku, "package_width", "packageWidth", "width"),
					AliExpressOverseasManagedProductHelper.findString(detail, "package_width", "packageWidth", "width")));
			putIfNotBlank(flatSku, "packageHeight", firstNotBlank(
					AliExpressOverseasManagedProductHelper.findString(sku, "package_height", "packageHeight", "height"),
					AliExpressOverseasManagedProductHelper.findString(detail, "package_height", "packageHeight", "height")));
			putIfNotBlank(flatSku, "grossWeight", firstNotBlank(
					AliExpressOverseasManagedProductHelper.findString(sku, "gross_weight", "grossWeight", "weight"),
					AliExpressOverseasManagedProductHelper.findString(detail, "gross_weight", "grossWeight", "weight")));
			flatSku.put("packageUnit", firstNotBlank(
					AliExpressOverseasManagedProductHelper.findString(sku, "package_unit", "packageUnit"),
					AliExpressOverseasManagedProductHelper.findString(detail, "package_unit", "packageUnit"),
					"cm"));
			flatSku.put("weightUnit", firstNotBlank(
					AliExpressOverseasManagedProductHelper.findString(sku, "weight_unit", "weightUnit"),
					AliExpressOverseasManagedProductHelper.findString(detail, "weight_unit", "weightUnit"),
					"kg"));
			flatSku.put("platformParentSpuNo", spuId);
			flatSku.put(DmpInputMongoHandler.MONGO_BASE_ID, childMongo.get(DmpInputMongoHandler.MONGO_BASE_ID));
			flatSku.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, childMongo.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
			result.add(flatSku);
		}
		return result;
	}

	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
		DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
		String parentStorageName = mainConvertId.getStorageName();
		ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
		Map<String, String> spuIdDmpIdMap = new HashMap<>();
		if (CollUtil.isNotEmpty(listMaps)) {
			for (Map<String, Object> listMap : listMaps) {
				Object spuId = listMap.get("spu_id");
				Object id = listMap.get(BaseEntity.FIELD_ID);
				if (spuId != null && id != null) {
					spuIdDmpIdMap.put(spuId.toString(), id.toString());
				}
			}
		}
		for (Map<String, Object> childEntity : dmpInputMongoChildEntityList) {
			Object spuId = childEntity.get("spuId");
			if (spuId != null) {
				childEntity.put(MAIN_ID, spuIdDmpIdMap.get(spuId.toString()));
			}
		}
	}

	private String firstNotBlank(String... values) {
		for (String value : values) {
			if (StringUtils.isNotBlank(value)) {
				return value;
			}
		}
		return "";
	}

	private void putIfNotBlank(Map<String, Object> data, String key, String value) {
		if (StringUtils.isNotBlank(value)) {
			data.put(key, value);
		}
	}
}
