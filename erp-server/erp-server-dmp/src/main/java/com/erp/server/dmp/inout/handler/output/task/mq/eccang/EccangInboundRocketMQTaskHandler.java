package com.erp.server.dmp.inout.handler.output.task.mq.eccang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformInboundDTO.Receiving;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.antu.dto.response.AntuReceiptResp;
import com.sdk.wms.antu.enums.AntuEnums;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class EccangInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

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

		platformInboundDTO.setReceivingStatus(AntuEnums.ReceivingStatusEnum.getInstockByCode(dmpThirdInboundEntity.getReceivingStatus()));
		List<AntuReceiptResp.Item> itemList = JSON.parseArray(dmpThirdInboundEntity.getDetailListJson(), AntuReceiptResp.Item.class);
		List<Receiving> receivingDataList = new ArrayList<>();

		LocalDateTime receiveTime = LocalDateTime.now();
		for(AntuReceiptResp.Item gcReceiving : itemList) {
			Receiving receiving = new Receiving();
			receiving.setProductSku(gcReceiving.getProductSku());
			receiving.setReceiveQty(gcReceiving.getPutawayQty());
			receiving.setReceiveTime(gcReceiving.getRdUpdateTime());
			receivingDataList.add(receiving);
		}
		platformInboundDTO.setReceivingDataList(receivingDataList);

		this.groupBySku(platformInboundDTO);

		return platformInboundDTO;
	}

    private void groupBySku(PlatformInboundDTO dto) {
        Map<String, Integer> receivedQuantityMap = dto.getReceivingDataList().stream()
                .collect(Collectors.groupingBy(Receiving::getProductSku, Collectors.summingInt(Receiving::getReceiveQty)));

        List<PlatformInboundDTO.Item> items = receivedQuantityMap.entrySet().stream()
                .map(entry -> new PlatformInboundDTO.Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        dto.setItems(items);
    }
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("receivingCode");
    }
}
