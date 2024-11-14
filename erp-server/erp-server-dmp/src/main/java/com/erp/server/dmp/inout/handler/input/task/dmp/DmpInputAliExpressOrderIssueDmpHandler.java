package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.service.DmpSoInfoService;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderIssueDmpHandler extends DmpInputDbConvertDmpHandler{
	@Autowired
	private DmpSoInfoService dmpSoInfoService;
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		if(!dmpInputDataDmpRelationMaps.isEmpty()) {
			Collection<List<TreeMap<String, Object>>> values = dmpInputDataDmpRelationMaps.values();
			if(CollUtil.isNotEmpty(values)) {
				List<String> orders = new ArrayList<>();
				for(List<TreeMap<String, Object>> v : values) {
					orders.addAll(v.stream().map(a -> a.get("parent_order_id").toString()).collect(Collectors.toList()));
				}
				Map<String, String> orderIdMaps = new HashMap<>();
				if(CollUtil.isNotEmpty(orders)) {
					List<DmpSoInfoEntity> list = dmpSoInfoService.lambdaQuery()
						.in(DmpSoInfoEntity::getThirdCode, orders)
						.eq(DmpSoInfoEntity::getSourcePlatform, DmpBasicSystemCodeEnum.ALI_EXPRESS.getCode())
						.eq(DmpSoInfoEntity::getNextLevelId, nextLevelId)
						.list();
					if(CollUtil.isEmpty(list)) {
						throw new ServiceException("所有退货订单未查询到订单数据");
					}
					orderIdMaps = list.stream().collect(Collectors.toMap(DmpSoInfoEntity::getThirdCode, DmpSoInfoEntity::getId , (v1 , v2) -> v1));
				}
				for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
					List<TreeMap<String, Object>> value = dmpInputDataDmpRelationMap.getValue();
					for(TreeMap<String, Object> v : value) {
						String parent_order_id = v.get("parent_order_id").toString();
						String sourceId = orderIdMaps.get(parent_order_id);
						if(StringUtils.isBlank(sourceId)) {
							throw new ServiceException("退货订单"+ parent_order_id +"未查询到订单数据");
						}
						v.put("sourceId", sourceId);
					}
				}
			}
		}
	}
	
}
