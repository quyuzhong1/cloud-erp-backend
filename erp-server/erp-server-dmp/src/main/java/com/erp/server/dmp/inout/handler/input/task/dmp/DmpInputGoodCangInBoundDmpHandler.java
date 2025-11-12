package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputGoodCangInBoundDmpHandler extends DmpInputDoChildDmpHandler{

	@Override
	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
		List<ParamData> paramDataList = new ArrayList<>();
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).list();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
		return mongoService.findMongoData(paramDataList, childMongoStorageName);
	}


	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
		DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
		String parentStorageName = mainConvertId.getStorageName();
		ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
		// 单号和店铺唯一
		Map<String, String> billNoIdMap = new HashMap<>();

		if (CollUtil.isNotEmpty(listMaps)) {
			for (Map<String, Object> listMap : listMaps) {
				String sourcePlatform = listMap.getOrDefault("source_platform", "").toString();
				String platformReturnOrderNo = listMap.getOrDefault("receiving_code", "").toString();
				String authId = listMap.getOrDefault(NEXT_LEVEL_ID, "").toString();
				// 唯一
				String uniqueId = CharSequenceUtil.format("{}_{}_{}", sourcePlatform, platformReturnOrderNo, authId);
				// 单号配店铺
				billNoIdMap.put(uniqueId, listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for (Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			String sourcePlatform = DmpBasicSystemCodeEnum.GOODCANG.getCode();
			String reference_no = dmpInputMongoChildEntity.get("receiving_code").toString();
			String authId = dmpInputMongoChildEntity.getOrDefault("nextLevelId", "").toString();
			String uniqueId = CharSequenceUtil.format("{}_{}_{}", sourcePlatform, reference_no, authId);
			String dmpId = billNoIdMap.get(uniqueId);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
			Map<String, Object> mongoData = mongoDataMaps.get(0);
			Object overseasDetail = mongoData.get("overseas_detail");
			if(overseasDetail != null) {
				for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
					dmpDataMap.put("detailListJson", JSON.toJSONString(overseasDetail));
				}
			}
		}
	}
}
