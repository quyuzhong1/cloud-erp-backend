package com.erp.server.dmp.inout.handler.output.task.mq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.DmpSoPrestockDetailDTO.PrestockDetailDTO;
import com.erp.model.dmp.dto.DmpSoPrestockInfoDTO.PrestockDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoPrestockDetailEntity;
import com.erp.model.dmp.entity.DmpSoPrestockInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputWdtPreStockRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpSoPrestockInfoEntity> dmpSoPrestockInfoEntityMap = new HashMap<>();
		Map<String, List<DmpSoPrestockDetailEntity>> dmpSoPrestockDetailEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_so_prestock_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoPrestockInfoEntity dmpSoPrestockInfoEntity = (DmpSoPrestockInfoEntity) v;
						dmpSoPrestockInfoEntityMap.put(dmpSoPrestockInfoEntity.getId(), dmpSoPrestockInfoEntity);
					}
				}else if("dmp_so_prestock_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoPrestockDetailEntity dmpSoPrestockDetailEntity = (DmpSoPrestockDetailEntity) v;
						String mainId = dmpSoPrestockDetailEntity.getMainId();
						List<DmpSoPrestockDetailEntity> list = dmpSoPrestockDetailEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoPrestockDetailEntity);
						dmpSoPrestockDetailEntityMap.put(mainId, list);
					}
				}
			}
		}
		
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
		Set<String> changeIds = new HashSet<>(); 
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_so_prestock_info".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}else if("dmp_so_prestock_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoPrestockDetailEntity dmpSoPrestockDetailEntity = (DmpSoPrestockDetailEntity) v;
						changeIds.add(dmpSoPrestockDetailEntity.getMainId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpSoPrestockInfoEntity dmpSoPrestockInfoEntity = dmpSoPrestockInfoEntityMap.get(changId);
			if(this.validateDataBlack(dmpSoPrestockInfoEntity, cfgOutputId)) {
				continue;
			}
			List<DmpSoPrestockDetailEntity> itemList = dmpSoPrestockDetailEntityMap.get(changId);
			PrestockDTO prestockDto = BeanUtil.copyProperties(dmpSoPrestockInfoEntity, PrestockDTO.class);
			List<PrestockDetailDTO> detailList = BeanUtil.copyToList(itemList, PrestockDetailDTO.class);
			detailList.forEach(d -> {
				String warehouseLocation = d.getWarehouseLocation();
				if(StringUtils.isNotBlank(warehouseLocation) && DmpOutputWdtSoOutstockRocketMQTaskHandler.nullPositionNo.contains(warehouseLocation)) {
					d.setWarehouseLocation("");
				}
			});
			prestockDto.setDetailList(detailList);
			map.put(changId, JSON.toJSONString(prestockDto));
		}
		return map;
	}
	
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("thirdCode");
    }

}
