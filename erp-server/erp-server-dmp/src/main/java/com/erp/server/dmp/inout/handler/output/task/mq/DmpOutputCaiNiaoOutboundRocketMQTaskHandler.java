package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.wms.aliexpress.enums.CaiNiaoEnums;
import io.seata.common.util.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class DmpOutputCaiNiaoOutboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpThirdOutboundEntity> dmpThirdOutboundEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_third_outbound".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpThirdOutboundEntity dmpThirdOutboundEntity = (DmpThirdOutboundEntity) v;
						dmpThirdOutboundEntityMap.put(dmpThirdOutboundEntity.getId(), dmpThirdOutboundEntity);
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
				if("dmp_third_outbound".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpThirdOutboundEntity dmpThirdOutboundEntity = dmpThirdOutboundEntityMap.get(changId);
			PlatformOutboundDTO platformOutboundDTO = this.convert(dmpThirdOutboundEntity, cfgOutputId);
			if(platformOutboundDTO != null) {
				map.put(dmpThirdOutboundEntity.getId(), JSON.toJSONString(platformOutboundDTO));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformOutboundDTO convert(DmpThirdOutboundEntity dmpThirdOutboundEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpThirdOutboundEntity, cfgOutputId)) {
    		return null;
    	}
    	String orderStatus = dmpThirdOutboundEntity.getOrderStatus();
		String erpOrderStatus = CaiNiaoEnums.OrderStatusEnum.getErpOrderStatus(orderStatus);
		if(StringUtils.isBlank(erpOrderStatus)) {
			return null;
		}
    	PlatformOutboundDTO platformOutboundDTO = BeanUtil.copyProperties(dmpThirdOutboundEntity, PlatformOutboundDTO.class);
    	String sourcePlatform = dmpThirdOutboundEntity.getSourcePlatform();
		platformOutboundDTO.setPlatform(sourcePlatform);
    	platformOutboundDTO.setProvider(sourcePlatform);
    	platformOutboundDTO.setOutBoundTime(dmpThirdOutboundEntity.getDateShipping());
		platformOutboundDTO.setOrderStatus(erpOrderStatus);
    	platformOutboundDTO.setThirdOrderStatus(CaiNiaoEnums.OrderStatusEnum.getName(orderStatus));
    	platformOutboundDTO.setTrackNo(dmpThirdOutboundEntity.getTrackingNo());
    	
        return platformOutboundDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("orderCode");
    }
}
