package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputKingdeeNextDmpHandler extends DmpInputDbConvertDmpHandler{
	
	protected DmpCfgInputConvertEntity parentDmpCfgInputConvertEntity;
	protected ServiceImpl parentServiceImpl;
	
	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		parentDmpCfgInputConvertEntity = this.getMainConvertId();
		parentServiceImpl = this.getServiceImpl(parentDmpCfgInputConvertEntity.getStorageName());
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
		Map<String, String> billNoIdMap = new HashMap<>();
		if(CollUtil.isNotEmpty(listMaps)) {
			String uniqueFieldName = StrUtils.underlineByhump(parentDmpCfgInputConvertEntity.getUniqueFieldName());
			for(Map<String, Object> listMap : listMaps) {
				billNoIdMap.put(listMap.get(uniqueFieldName).toString(), listMap.get(BaseEntity.ID).toString());
			}
		}
		
		Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData = super.convertData(dmpInputMongoEntityList);
		String mainId = StrUtils.underlineToCamel(MAIN_ID, true);
		
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> c : convertData.entrySet()) {
			for(Map<String, Object> dmpInputMongoNextEntity : c.getKey()) {
				String billNo = dmpInputMongoNextEntity.get("FBillNo").toString();
				String mainIdValue = billNoIdMap.get(billNo);
				if(StringUtils.isBlank(mainIdValue)) {
					c.getValue().removeIf(v -> true);
				}else {
					c.getValue().forEach(v -> v.put(mainId, mainIdValue));
				}
			}
		}
		return convertData;
	}
	
	
}
