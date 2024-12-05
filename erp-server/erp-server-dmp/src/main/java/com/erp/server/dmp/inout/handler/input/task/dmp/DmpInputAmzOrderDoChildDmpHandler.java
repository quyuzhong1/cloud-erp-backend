package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharSequenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dmp处理子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
@Slf4j
public class DmpInputAmzOrderDoChildDmpHandler extends DmpInputDoChildDmpHandler{
	
	@Override
	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList , String childMongoStorageName){
		List<ParamData> paramDataList = new ArrayList<>();
		String childId = this.getDmpCfgInputChildId();
		if(StringUtils.isBlank(childId)) {
			throw new ServiceException("未查询到DmpInputAmzOrderDoChildDmpHandler子类id");
		}
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).eq(DmpInputTaskEntity::getCfgInputId, childId).list();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
		List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, childMongoStorageName);
		return this.afterDoDmpInputMongoChildEntityList(dmpInputMongoChildList);
	}
	
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList){
		return dmpInputMongoChildList;
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
				String thirdCode = listMap.getOrDefault("third_code", "").toString();
				String shopId = listMap.getOrDefault("shop_id", "").toString();
				String uniqueId = CharSequenceUtil.format("{}_{}", thirdCode, shopId);
				billNoIdMap.put(uniqueId, listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			String thirdCode = dmpInputMongoChildEntity.get("amazonOrderId").toString();
			String shopId = dmpInputMongoChildEntity.get("shopId").toString();
			String uniqueId = CharSequenceUtil.format("{}_{}", thirdCode, shopId);
			String dmpId = billNoIdMap.get(uniqueId);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}


	/**
	 * 校验和获取指定字段
	 */
	protected String checkAndGetMongoValue(Map<String, Object> nongoObjectMap, String mongoFieldName) {
		Object reportDocumentIdObj = nongoObjectMap.get(mongoFieldName);
		if (null == reportDocumentIdObj) {
			String msg = StrUtil.format("未找到{}:taskId={}", mongoFieldName, dmpInputTaskEntity.getId());
			ServiceException.runError(msg);
		}
		return (String) reportDocumentIdObj;
	}
}
