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
import com.common.business.dto.PlatformCityDictDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdRegionEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputImlRegionRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpThirdRegionEntity> dmpThirdRegionEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_third_region".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpThirdRegionEntity dmpThirdRegionEntity = (DmpThirdRegionEntity) v;
						dmpThirdRegionEntityMap.put(dmpThirdRegionEntity.getId(), dmpThirdRegionEntity);
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
				if("dmp_third_region".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpThirdRegionEntity dmpThirdRegionEntity = dmpThirdRegionEntityMap.get(changId);
			PlatformCityDictDTO warehouse = this.convert(dmpThirdRegionEntity, cfgOutputId);
			if(warehouse != null) {
				map.put(dmpThirdRegionEntity.getId(), JSON.toJSONString(warehouse));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformCityDictDTO convert(DmpThirdRegionEntity dmpThirdRegionEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpThirdRegionEntity, cfgOutputId)) {
    		return null;
    	}
    	PlatformCityDictDTO warehouse = BeanUtil.copyProperties(dmpThirdRegionEntity, PlatformCityDictDTO.class);
    	String sourcePlatform = dmpThirdRegionEntity.getSourcePlatform();
		warehouse.setPlatform(sourcePlatform);
    	warehouse.setProvider(sourcePlatform);
    	
        return warehouse;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("regionId");
    }
}
