package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 亚马逊Listing下一步字段映射转换
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportParseProductNextDmpHandler extends DmpInputDoNextDmpHandler {

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		return Collections.singletonList(dmpInputMongoEntity);
	}


	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		log.debug("DmpInputAmzReportParseProductNextDmpHandler afterConvertData 处理");
	}


//	@Override
//	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
//		List<ParamData> paramDataList = new ArrayList<>();
//		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).list();
//		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
//		return mongoService.findMongoData(paramDataList, childMongoStorageName);
//	}
//
//	@Override
//	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
//		DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
//		String parentStorageName = mainConvertId.getStorageName();
//		ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
//		QueryWrapper<?> wrapper = new QueryWrapper<>();
//		wrapper.eq(INPUT_TASK_ID, inputTaskId);
//		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
//		Map<String, String> billNoIdMap = new HashMap<>();
//		if (CollUtil.isNotEmpty(listMaps)) {
//			for (Map<String, Object> listMap : listMaps) {
//				billNoIdMap.put(listMap.get("sku_id").toString(), listMap.get(BaseEntity.ID).toString());
//			}
//		}
//		for (Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
//			String billNo = dmpInputMongoChildEntity.get("listingId").toString();
//			String dmpId = billNoIdMap.get(billNo);
//			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
//		}
//	}
}
