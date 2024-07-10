package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputKingdeeOutstockDmpHandler extends DmpInputDbConvertDmpHandler{
	
	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertDataMap = super.convertData(dmpInputMongoEntityList);
		Set<String> thirdCodeSet = new HashSet<>();
		String thirdCodeName = StrUtils.underlineToCamel(DmpSoOutstockEntity.THIRD_CODE, true);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData : convertDataMap.entrySet()) {
			thirdCodeSet.addAll(convertData.getValue().stream().map(c -> c.get(thirdCodeName).toString()).collect(Collectors.toSet()));
		}
		
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.in(DmpSoOutstockEntity.THIRD_CODE, thirdCodeSet);
		wrapper.ne(DmpSoOutstockEntity.SOURCE_SYSTEM, DmpBasicSystemCodeEnum.KINGDEE.getCode());
		wrapper.select(DmpSoOutstockEntity.THIRD_CODE);
		List<Map<String, Object>> listMaps = dmpEntityServiceImpl.listMaps(wrapper);
		if(CollUtil.isNotEmpty(listMaps)) {
			Set<String> newThirdCodeName = listMaps.stream().map(l -> l.get(DmpSoOutstockEntity.THIRD_CODE).toString()).collect(Collectors.toSet());
			for(List<TreeMap<String, Object>> convertData : convertDataMap.values()) {
				convertData.removeIf(c -> newThirdCodeName.contains(c.get(thirdCodeName).toString()));
			}
		}
		
		return convertDataMap;
	}
	
	
}
