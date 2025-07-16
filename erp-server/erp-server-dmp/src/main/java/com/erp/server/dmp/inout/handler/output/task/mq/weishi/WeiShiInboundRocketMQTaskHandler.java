
package com.erp.server.dmp.inout.handler.output.task.mq.weishi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformInboundDTO.Receiving;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.jifeng.dto.response.JiFengInboundResp;
import com.sdk.wms.weishi.dto.response.WeiShiInboundResp;
import io.seata.common.util.StringUtils;
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
public class WeiShiInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

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
		List<WeiShiInboundResp.RowsDTO.InboundBoxListDTO> boxListDTOS = JSON.parseArray(dmpThirdInboundEntity.getDetailListJson(), WeiShiInboundResp.RowsDTO.InboundBoxListDTO.class);

		platformInboundDTO.setReceivingStatus(this.convertStatus(dmpThirdInboundEntity.getReceivingStatus(),boxListDTOS));
		if(OverseasInstockStatusEnum.TO_BE_SIGNED.getCode().equals(platformInboundDTO.getReceivingStatus())){
			return null;
		}
		List<Receiving> receivingDataList = new ArrayList<>();
    	for(WeiShiInboundResp.RowsDTO.InboundBoxListDTO skuListDTO : boxListDTOS) {
			if(!"PUTAWAY_FULL".equals(skuListDTO.getStatus()) || StringUtils.isBlank(skuListDTO.getFinishPutawayTime())){
				continue;
			}
			for (WeiShiInboundResp.RowsDTO.InboundBoxListDTO.InboundSkuListDTO inboundSkuListDTO : skuListDTO.getInboundSkuList()) {
				Receiving receiving = new Receiving();
				// 解析为 LocalDateTime
				LocalDateTime localDateTime = LocalDateTimeUtil.parse(skuListDTO.getFinishPutawayTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
				// 假设原时间为 UTC，绑定时区
				ZonedDateTime utcZoned = localDateTime.atZone(ZoneId.of("UTC"));
				// 转换为系统默认时区
				ZonedDateTime systemZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault());
				// 获取系统时区的 LocalDateTime
				LocalDateTime receiveTime = systemZoned.toLocalDateTime();
				platformInboundDTO.setDownloadTime(receiveTime);
				receiving.setProductSku(inboundSkuListDTO.getSkuCode());
				receiving.setReceiveQty(inboundSkuListDTO.getPutawayQty());
				receiving.setReceiveTime(receiveTime);
				receivingDataList.add(receiving);
			}
    	}
		if(CollectionUtils.isEmpty(receivingDataList)) {
			return null;
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

	private String convertStatus(String status,List<WeiShiInboundResp.RowsDTO.InboundBoxListDTO> boxListDTOS) {
		if("PUTAWAY_FULL".equals(status)){
			return OverseasInstockStatusEnum.SIGNED.getCode();
		}else if("IN_OPERATION".equals(status) && CollectionUtils.isNotEmpty(boxListDTOS) && boxListDTOS.stream().anyMatch(v->"PUTAWAY_FULL".equals(v.getStatus()))){
			return OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode();
		}else{
			return OverseasInstockStatusEnum.TO_BE_SIGNED.getCode();
		}
	}

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("receivingCode");
    }

}
