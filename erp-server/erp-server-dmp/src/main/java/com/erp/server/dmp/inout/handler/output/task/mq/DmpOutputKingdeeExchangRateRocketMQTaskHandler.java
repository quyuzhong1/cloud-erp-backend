package com.erp.server.dmp.inout.handler.output.task.mq;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.DmpExchangeRateDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdExchangeRateEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputKingdeeExchangRateRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{
	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpThirdExchangeRateEntity> dmpThirdExchangeRateEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_third_exchange_rate".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpThirdExchangeRateEntity dmpThirdExchangeRateEntity = (DmpThirdExchangeRateEntity) v;
						dmpThirdExchangeRateEntityMap.put(dmpThirdExchangeRateEntity.getId(), dmpThirdExchangeRateEntity);
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
				if("dmp_third_exchange_rate".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpThirdExchangeRateEntity dmpThirdExchangeRateEntity = dmpThirdExchangeRateEntityMap.get(changId);
			DmpExchangeRateDTO dto = this.convert(dmpThirdExchangeRateEntity, cfgOutputId);
			if(dto != null) {
				map.put(dmpThirdExchangeRateEntity.getId(), JSON.toJSONString(dto));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public DmpExchangeRateDTO convert(DmpThirdExchangeRateEntity dmpThirdExchangeRateEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpThirdExchangeRateEntity, cfgOutputId)) {
    		return null;
    	}
    	DmpExchangeRateDTO resultEntity = new DmpExchangeRateDTO();
    	
    	resultEntity.setType(dmpThirdExchangeRateEntity.getType());
        resultEntity.setSourceCurrencyCode(dmpThirdExchangeRateEntity.getSourceCurrencyCode());
        resultEntity.setTargetCurrencyCode(dmpThirdExchangeRateEntity.getTargetCurrencyCode());
        resultEntity.setExchangeRate(dmpThirdExchangeRateEntity.getExchangeRate());
        resultEntity.setIndirectExchangeRate(dmpThirdExchangeRateEntity.getIndirectExchangeRate());
        resultEntity.setSettlementDateBegin(dmpThirdExchangeRateEntity.getSettlementDateBegin().toLocalDate());
        resultEntity.setSettlementDateEnd(dmpThirdExchangeRateEntity.getSettlementDateEnd().toLocalDate());
        resultEntity.setSourceId(dmpThirdExchangeRateEntity.getSourceId());
        resultEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        resultEntity.setApproveStatus(dmpThirdExchangeRateEntity.getApproveStatus());
        LocalDateTime approveDate = dmpThirdExchangeRateEntity.getApproveDate();
        if(approveDate != null) {
        	resultEntity.setApproveDate(approveDate.toLocalDate());
        }
        LocalDateTime disabledDate = dmpThirdExchangeRateEntity.getDisabledDate();
        if(disabledDate != null) {
        	resultEntity.setDisabledDate(disabledDate.toLocalDate());
        }
        resultEntity.setDisabled(dmpThirdExchangeRateEntity.getDisabled());
    	
        return resultEntity;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("sourceId");
    }
}
