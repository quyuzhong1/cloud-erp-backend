package com.erp.server.dmp.inout.handler.output.task.mq;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.erp.model.dmp.entity.DmpThirdInventoryTransFlowEntity;
import com.sdk.wms.goodcang.dto.response.GoodCangReceiptBatchResp;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformInboundDTO.Receiving;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.wms.goodcang.enums.GoodCangEnums;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputGoodCangInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpThirdInboundEntity> dmpThirdInboundEntityMap = new HashMap<>();
		Map<String, DmpThirdInventoryTransFlowEntity> dmpFlowEntityMap = new HashMap<>();


		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_third_inbound".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpThirdInboundEntity dmpThirdInboundEntity = (DmpThirdInboundEntity) v;
						dmpThirdInboundEntityMap.put(dmpThirdInboundEntity.getId(), dmpThirdInboundEntity);
					}
				} else if ("dmp_third_inventory_trans_flow".equals(storageName)) {
				    for (BaseEntity v : value) {
				    	DmpThirdInventoryTransFlowEntity flowEntity = (DmpThirdInventoryTransFlowEntity) v;
				    	dmpFlowEntityMap.put(flowEntity.getId(), flowEntity);
				    }
			    }
			}
		}

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_third_inventory_trans_flow".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }
            }
        }
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
            // 流水维度推送
            DmpThirdInventoryTransFlowEntity dmpFlowEntity = dmpFlowEntityMap.get(changId);
            PlatformInboundDTO dto = this.convert(dmpFlowEntity, dmpThirdInboundEntityMap.get(dmpFlowEntity.getMainId()), cfgOutputId);
            if (null != dto) {
                map.put(dmpFlowEntity.getId(), JSON.toJSONString(dto));
            }
		}
		return map;
	}

	/**
     * 解析订单数据
     **/
    public PlatformInboundDTO convert(DmpThirdInventoryTransFlowEntity dmpFlowEntity, DmpThirdInboundEntity dmpThirdInboundEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpFlowEntity, cfgOutputId)) {
    		return null;
    	}
    	PlatformInboundDTO platformInboundDTO = BeanUtil.copyProperties(dmpThirdInboundEntity, PlatformInboundDTO.class);
    	String sourcePlatform = dmpThirdInboundEntity.getSourcePlatform();
		platformInboundDTO.setAuthId(dmpFlowEntity.getAuthId());
		platformInboundDTO.setHasReceivedData(true);
		platformInboundDTO.setPlatform(sourcePlatform);
    	platformInboundDTO.setProvider(sourcePlatform);
    	platformInboundDTO.setDownloadTime(LocalDateTime.now());
    	platformInboundDTO.setReceivingStatus(GoodCangEnums.OpenReceivingStatusEnum.getInstockByCode(Integer.valueOf(dmpThirdInboundEntity.getReceivingStatus())));
    	List<GoodCangReceiptBatchResp.GcReceiving> gcReceivingList = JSON.parseArray(dmpThirdInboundEntity.getDetailListJson(), GoodCangReceiptBatchResp.GcReceiving.class);
    	List<Receiving> receivingDataList = new ArrayList<>();
    	
    	LocalDateTime receiveTime = LocalDateTime.now();
    	for(GoodCangReceiptBatchResp.GcReceiving gcReceiving : gcReceivingList) {
    		Receiving receiving = new Receiving();
    		receiving.setProductSku(gcReceiving.getProductSku());
    		receiving.setReceiveQty(gcReceiving.getReceivedQty());
    		receiving.setReceiveTime(receiveTime);
    		receivingDataList.add(receiving);
    	}
		platformInboundDTO.setReceivingDataList(receivingDataList);
		
		this.groupBySku(platformInboundDTO);

		// 重新按流水生成签收
		Receiving receiving = new Receiving();
		receiving.setProductSku(dmpFlowEntity.getProductSku());
		receiving.setReceiveQty(dmpFlowEntity.getQty());
		receiving.setReceiveTime(dmpFlowEntity.getTradeTime());
		receiving.setThirdId(dmpFlowEntity.getThirdId());
		platformInboundDTO.setReceivingDataList(Collections.singletonList(receiving));

		return platformInboundDTO;
    }

    private void groupBySku(PlatformInboundDTO dto) {
        Map<String, Integer> receivedQuantityMap = dto.getReceivingDataList().stream()
                .collect(Collectors.groupingBy(PlatformInboundDTO.Receiving::getProductSku, Collectors.summingInt(PlatformInboundDTO.Receiving::getReceiveQty)));

        List<PlatformInboundDTO.Item> items = receivedQuantityMap.entrySet().stream()
                .map(entry -> new PlatformInboundDTO.Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        dto.setItems(items);
    }
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Collections.singletonList("receivingCode");
    }
}
