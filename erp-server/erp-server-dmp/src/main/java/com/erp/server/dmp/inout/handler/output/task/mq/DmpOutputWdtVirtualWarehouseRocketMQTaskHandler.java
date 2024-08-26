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
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdWarehouseInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.wangdian.dto.ErpVirtualWarehouseDto;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputWdtVirtualWarehouseRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{
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
			ErpVirtualWarehouseDto warehouse = this.convert(dmpThirdWarehouseInfoEntity, cfgOutputId);
			if(warehouse != null) {
				map.put(dmpThirdWarehouseInfoEntity.getId(), JSON.toJSONString(warehouse));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public ErpVirtualWarehouseDto convert(DmpThirdWarehouseInfoEntity dmpThirdWarehouseInfoEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpThirdWarehouseInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	ErpVirtualWarehouseDto warehouse = new ErpVirtualWarehouseDto();
    	String authId = dmpThirdWarehouseInfoEntity.getAuthId();
    	warehouse.setUniqueId(authId);
        warehouse.setDisabled(dmpThirdWarehouseInfoEntity.getDisabled());
        warehouse.setSysType(PlatformDictEnum.WDT.getCode());
		warehouse.setWarehouseId(authId);
        warehouse.setCode(dmpThirdWarehouseInfoEntity.getWarehouseCode());
        warehouse.setName(dmpThirdWarehouseInfoEntity.getWarehouseName());
        warehouse.setCreated(LocalDateUtil.formatTime(dmpThirdWarehouseInfoEntity.getPlatformCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        warehouse.setModified(LocalDateUtil.formatTime(dmpThirdWarehouseInfoEntity.getPlatformUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
        warehouse.setRemark(dmpThirdWarehouseInfoEntity.getRemark());
        warehouse.setWarehouseList(dmpThirdWarehouseInfoEntity.getWarehouseList());
    	
        return warehouse;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("code");
    }
}