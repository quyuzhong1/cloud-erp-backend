package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressSkuDmpHandler extends DmpInputDoChildDmpHandler{
	
	@Override
	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList , String childMongoStorageName){
		List<Map<String, Object>> dmpInputMongoChildEntityList = new ArrayList<>();
		List<ParamData> paramDataList = new ArrayList<>();
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).list();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
		List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, childMongoStorageName);
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
				Object aeop_ae_product_s_k_us = dmpInputMongoChild.get("aeop_ae_product_s_k_us");
				if(aeop_ae_product_s_k_us != null) {
					Map<String, Object> aeop_ae_product_s_k_us_map = (Map<String, Object>) aeop_ae_product_s_k_us;
					List<Map<String, Object>> aeop_ae_product_sku_list = (List<Map<String, Object>>)aeop_ae_product_s_k_us_map.get("aeop_ae_product_sku");
					aeop_ae_product_sku_list.forEach(l -> {
						l.put("platformCreateTime", dmpInputMongoChild.get("gmt_create"));
						l.put("platformUpdateTime", dmpInputMongoChild.get("gmt_modified"));
						l.put("spuId", dmpInputMongoChild.get("product_id"));
						l.put("imageUrls", dmpInputMongoChild.get("image_u_r_ls"));
						l.put("packageLength", dmpInputMongoChild.get("package_length"));
						l.put("packageWidth", dmpInputMongoChild.get("package_width"));
						l.put("packageHeight", dmpInputMongoChild.get("package_height"));
						l.put("grossWeight", dmpInputMongoChild.get("gross_weight"));
						l.put(DmpInputMongoHandler.MONGO_BASE_ID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_ID));
						l.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
					});
					dmpInputMongoChildEntityList.addAll(aeop_ae_product_sku_list);
				}
			}
		}
		return dmpInputMongoChildEntityList;
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
			String billNo = dmpInputMongoChildEntity.get("spuId").toString();
			String dmpId = billNoIdMap.get(billNo);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}
	
}
