
package com.erp.server.dmp.inout.handler.output.task.mq.damai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformInboundDTO.Receiving;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.damai.dto.response.DaMaiBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiInventoryTransResp;
import com.sdk.wms.goodcang.dto.response.GoodCangOutboundResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.jifeng.dto.response.JiFengInboundResp;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class DaMaiInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

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
		DaMaiInventoryTransResp respDto = JSON.parseObject(dmpThirdInboundEntity.getDetailListJson(), DaMaiInventoryTransResp.class);
		List<Receiving> receivingDataList = new ArrayList<>();
		Receiving receiving = new Receiving();
		// 解析为 LocalDateTime
		LocalDateTime localDateTime = LocalDateTime.parse(respDto.getOperationTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		// 假设原时间为 UTC，绑定时区
		ZonedDateTime utcZoned = localDateTime.atZone(ZoneId.of("UTC"));
		// 转换为系统默认时区
		ZonedDateTime systemZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault());
		// 获取系统时区的 LocalDateTime
		LocalDateTime receiveTime = systemZoned.toLocalDateTime();
		platformInboundDTO.setDownloadTime(receiveTime);
		receiving.setProductSku(respDto.getCustomerSkuCode());
		receiving.setReceiveQty(respDto.getTransQty());
		receiving.setReceiveTime(receiveTime);
		receiving.setThirdId(dmpThirdInboundEntity.getSourceCode());
		receivingDataList.add(receiving);
		platformInboundDTO.setReceivingStatus(OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode());
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
