package com.erp.server.dmp.inout.handler.output.task.mq;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInventoryDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInventoryEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputGoodCangInventoryRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpThirdInventoryEntity> dmpThirdInventoryEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_third_inventory".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpThirdInventoryEntity dmpThirdInventoryEntity = (DmpThirdInventoryEntity) v;
						dmpThirdInventoryEntityMap.put(dmpThirdInventoryEntity.getId(), dmpThirdInventoryEntity);
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
				if("dmp_third_inventory".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpThirdInventoryEntity dmpThirdInventoryEntity = dmpThirdInventoryEntityMap.get(changId);
			PlatformInventoryDTO platformInventoryDTO = this.convert(dmpThirdInventoryEntity, cfgOutputId);
			if(platformInventoryDTO != null) {
				map.put(dmpThirdInventoryEntity.getId(), JSON.toJSONString(platformInventoryDTO));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformInventoryDTO convert(DmpThirdInventoryEntity dmpThirdInventoryEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpThirdInventoryEntity, cfgOutputId)) {
    		return null;
    	}
    	PlatformInventoryDTO platformInventoryDTO = BeanUtil.copyProperties(dmpThirdInventoryEntity, PlatformInventoryDTO.class);
    	String sourcePlatform = dmpThirdInventoryEntity.getSourcePlatform();
		platformInventoryDTO.setPlatform(sourcePlatform);
    	platformInventoryDTO.setProvider(sourcePlatform);
    	platformInventoryDTO.setProviderErpId(dmpThirdInventoryEntity.getAuthId());
    	platformInventoryDTO.setDownloadTime(LocalDateTime.now());
    	
        return platformInventoryDTO;
    }
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("platformWarehouseCode" , "productSku");
    }

}
