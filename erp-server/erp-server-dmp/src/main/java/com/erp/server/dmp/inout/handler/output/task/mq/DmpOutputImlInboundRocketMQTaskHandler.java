package com.erp.server.dmp.inout.handler.output.task.mq;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInboundDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.wms.iml.enums.ImlEnums;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputImlInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpThirdInboundEntity> dmpThirdInboundEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_third_inbound".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpThirdInboundEntity dmpThirdInboundEntity = (DmpThirdInboundEntity) v;
						dmpThirdInboundEntityMap.put(dmpThirdInboundEntity.getId(), dmpThirdInboundEntity);
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
				if("dmp_third_inbound".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpThirdInboundEntity dmpThirdInboundEntity = dmpThirdInboundEntityMap.get(changId);
			PlatformInboundDTO platformInboundDTO = this.convert(dmpThirdInboundEntity, cfgOutputId);
			if(platformInboundDTO != null) {
				map.put(dmpThirdInboundEntity.getId(), JSON.toJSONString(platformInboundDTO));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformInboundDTO convert(DmpThirdInboundEntity dmpThirdInboundEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpThirdInboundEntity, cfgOutputId)) {
    		return null;
    	}
    	PlatformInboundDTO platformInboundDTO = BeanUtil.copyProperties(dmpThirdInboundEntity, PlatformInboundDTO.class);
    	String sourcePlatform = dmpThirdInboundEntity.getSourcePlatform();
		platformInboundDTO.setPlatform(sourcePlatform);
    	platformInboundDTO.setProvider(sourcePlatform);
    	platformInboundDTO.setDownloadTime(LocalDateTime.now());
    	platformInboundDTO.setReceivingStatus(ImlEnums.ReceivingStatusEnum.getInstockByCode(dmpThirdInboundEntity.getReceivingStatus()));
    	List<com.sdk.wms.iml.dto.response.ImlReceiptResp.Item> items = JSON.parseArray(dmpThirdInboundEntity.getDetailListJson(), com.sdk.wms.iml.dto.response.ImlReceiptResp.Item.class);
    	List<com.common.business.dto.PlatformInboundDTO.Item> itemList = new ArrayList<>();
    	
    	for(com.sdk.wms.iml.dto.response.ImlReceiptResp.Item item : items) {
    		com.common.business.dto.PlatformInboundDTO.Item i = new com.common.business.dto.PlatformInboundDTO.Item();
    		i.setProductSku(item.getProductSku());
    		i.setReceivedQuantity(item.getReceivedQuantity());
    		i.setPutawayQuantity(item.getPutawayQuantity());
    		Integer boxNo = item.getBoxNo();
    		if(boxNo != null) {
    			i.setBoxNo(boxNo.toString());
    		}
    		itemList.add(i);
    	}
    	
		platformInboundDTO.setItems(itemList);
    	
        return platformInboundDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("receivingCode");
    }
}
