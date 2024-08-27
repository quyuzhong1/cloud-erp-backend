package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformTransferWarehouseDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpTransferWarehouseInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class AntuTransferRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpTransferWarehouseInfoEntity> dmpTransferWarehouseInfoEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_transfer_warehouse_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpTransferWarehouseInfoEntity dmpTransferWarehouseInfoEntity = (DmpTransferWarehouseInfoEntity) v;
						dmpTransferWarehouseInfoEntityMap.put(dmpTransferWarehouseInfoEntity.getId(), dmpTransferWarehouseInfoEntity);
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
				if("dmp_transfer_warehouse_info".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpTransferWarehouseInfoEntity dmpTransferWarehouseInfoEntity = dmpTransferWarehouseInfoEntityMap.get(changId);
			PlatformTransferWarehouseDTO warehouse = this.convert(dmpTransferWarehouseInfoEntity, cfgOutputId);
			if(warehouse != null) {
				map.put(dmpTransferWarehouseInfoEntity.getId(), JSON.toJSONString(warehouse));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformTransferWarehouseDTO convert(DmpTransferWarehouseInfoEntity dmpTransferWarehouseInfoEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpTransferWarehouseInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	PlatformTransferWarehouseDTO warehouse = BeanUtil.copyProperties(dmpTransferWarehouseInfoEntity, PlatformTransferWarehouseDTO.class);
    	String sourcePlatform = dmpTransferWarehouseInfoEntity.getSourcePlatform();
		warehouse.setPlatform(sourcePlatform);
    	warehouse.setProvider(sourcePlatform);
    	warehouse.setProviderErpId(dmpTransferWarehouseInfoEntity.getAuthId());
    	
        return warehouse;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("logisticsChannelCode" , "transferWarehouseCode" , "destinationWarehouseCode");
    }
}
