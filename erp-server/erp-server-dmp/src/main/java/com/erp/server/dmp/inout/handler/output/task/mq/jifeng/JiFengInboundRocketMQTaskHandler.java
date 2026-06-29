package com.erp.server.dmp.inout.handler.output.task.mq.jifeng;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.PlatformInboundDTO.Receiving;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdInboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.goodcang.dto.response.GoodCangReceiptBatchResp.GcReceiving;
import com.sdk.wms.goodcang.enums.GoodCangEnums;
import com.sdk.wms.jifeng.dto.response.JiFengInboundResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class JiFengInboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

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
		List<JiFengInboundResp.SkuListDTO> boxListDTOS = JSON.parseArray(dmpThirdInboundEntity.getDetailListJson(), JiFengInboundResp.SkuListDTO.class);

		platformInboundDTO.setReceivingStatus(this.convertStatus(Integer.valueOf(dmpThirdInboundEntity.getReceivingStatus()),boxListDTOS));
		List<Receiving> receivingDataList = new ArrayList<>();


    	for(JiFengInboundResp.SkuListDTO skuListDTO : boxListDTOS) {
    		Receiving receiving = new Receiving();
			// 解析为 LocalDateTime
			LocalDateTime localDateTime = LocalDateTime.parse(skuListDTO.getPutawayLastTime());
			// 假设原时间为 UTC，绑定时区
			ZonedDateTime utcZoned = localDateTime.atZone(ZoneId.of("UTC"));
			// 转换为系统默认时区
			ZonedDateTime systemZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault());
			// 获取系统时区的 LocalDateTime
			LocalDateTime receiveTime = systemZoned.toLocalDateTime();
			platformInboundDTO.setDownloadTime(receiveTime);
    		receiving.setProductSku(skuListDTO.getSku());
    		receiving.setReceiveQty(Objects.isNull(skuListDTO.getPutawayCount())?0:skuListDTO.getPutawayCount());
    		receiving.setReceiveTime(receiveTime);
    		receivingDataList.add(receiving);
    	}
		platformInboundDTO.setReceivingDataList(receivingDataList);
		
		this.groupBySku(platformInboundDTO, boxListDTOS);
    	
        return platformInboundDTO;
    }

    /**
     * 按 SKU 汇总收货/良品/不良品数量。
     * <p>
     * 极风入库单中同一 SKU 满足 {@code putawayCount == goodCount + badCount}，
     * 因此 {@code receivedQuantity} 仍沿用 {@code putawayCount} 汇总（语义不变），
     * 额外把 {@code goodCount}/{@code badCount} 汇总进 {@code goodQuantity}/{@code badQuantity}，
     * 供下游 {@code handlePlatformMessage} 按良品/不良品拆分生成签收记录。
     */
    private void groupBySku(PlatformInboundDTO dto, List<JiFengInboundResp.SkuListDTO> skuList) {
        Map<String, PlatformInboundDTO.Item> itemMap = new LinkedHashMap<>();
        for (JiFengInboundResp.SkuListDTO skuListDTO : skuList) {
            int putaway = Objects.isNull(skuListDTO.getPutawayCount()) ? 0 : skuListDTO.getPutawayCount();
            int good = Objects.isNull(skuListDTO.getGoodCount()) ? 0 : skuListDTO.getGoodCount();
            int bad = Objects.isNull(skuListDTO.getBadCount()) ? 0 : skuListDTO.getBadCount();
            // 极风上架数应满足 putawayCount == goodCount + badCount。一旦平台回传不一致，
            // 下游 WMS 明细签收数取 putaway 汇总、良品/不良品流水取 good/bad 汇总，会造成
            // 「明细签收总数 ≠ 良品流水 + 不良品流水」的静默数据偏差，此处打 warn 暴露异常数据，便于排查。
            if (putaway != good + bad) {
                log.warn("极风入库上架数与良品/不良品数不一致 receivingCode={}, sku={}, putawayCount={}, goodCount={}, badCount={}",
                        dto.getReceivingCode(), skuListDTO.getSku(), putaway, good, bad);
            }
            PlatformInboundDTO.Item item = itemMap.computeIfAbsent(skuListDTO.getSku(), sku -> {
                PlatformInboundDTO.Item newItem = new PlatformInboundDTO.Item(sku, 0);
                newItem.setGoodQuantity(0);
                newItem.setBadQuantity(0);
                return newItem;
            });
            item.setReceivedQuantity(item.getReceivedQuantity() + putaway);
            item.setGoodQuantity(item.getGoodQuantity() + good);
            item.setBadQuantity(item.getBadQuantity() + bad);
        }
        dto.setItems(new ArrayList<>(itemMap.values()));
    }

	private String convertStatus(Integer status,List<JiFengInboundResp.SkuListDTO> boxListDTOS) {
		if(status == 4){
			return OverseasInstockStatusEnum.SIGNED.getCode();
		}else if(status == 3 && CollectionUtils.isNotEmpty(boxListDTOS) && boxListDTOS.stream().anyMatch(v->v.getPutawayCount() > 0)){
			return OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode();
		}else if (status == 0){
			return OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode();
		}else{
			return OverseasInstockStatusEnum.TO_BE_SIGNED.getCode();
		}
	}
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("receivingCode");
    }
}
