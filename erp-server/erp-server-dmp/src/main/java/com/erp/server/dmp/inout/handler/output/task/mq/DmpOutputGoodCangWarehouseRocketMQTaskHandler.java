package com.erp.server.dmp.inout.handler.output.task.mq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformWarehouseDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdWarehouseInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputGoodCangWarehouseRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpThirdWarehouseInfoEntity> dmpThirdWarehouseInfoEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_third_warehouse_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity = (DmpThirdWarehouseInfoEntity) v;
						dmpThirdWarehouseInfoEntityMap.put(dmpThirdWarehouseInfoEntity.getId(), dmpThirdWarehouseInfoEntity);
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
				if("dmp_third_warehouse_info".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity = dmpThirdWarehouseInfoEntityMap.get(changId);
			PlatformWarehouseDTO warehouse = this.convert(dmpThirdWarehouseInfoEntity, cfgOutputId);
			if(warehouse != null) {
				map.put(dmpThirdWarehouseInfoEntity.getId(), JSON.toJSONString(warehouse));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformWarehouseDTO convert(DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpThirdWarehouseInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	PlatformWarehouseDTO warehouse = new PlatformWarehouseDTO();
    	warehouse.setWarehousePlatformType(dmpThirdWarehouseInfoEntity.getWarehousePlatformType());
    	String sourcePlatform = dmpThirdWarehouseInfoEntity.getSourcePlatform();
		warehouse.setPlatform(sourcePlatform);
    	warehouse.setProvider(sourcePlatform);
    	warehouse.setWarehouseCode(dmpThirdWarehouseInfoEntity.getWarehouseCode());
    	warehouse.setWarehouseName(dmpThirdWarehouseInfoEntity.getWarehouseName());
    	warehouse.setCountryCode(dmpThirdWarehouseInfoEntity.getCountryCode());
    	warehouse.setProviderErpId(dmpThirdWarehouseInfoEntity.getAuthId());
    	
        return warehouse;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("warehouseCode");
    }
}
