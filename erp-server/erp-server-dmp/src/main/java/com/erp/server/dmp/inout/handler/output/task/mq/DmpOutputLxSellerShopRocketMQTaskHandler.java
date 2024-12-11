package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.wangdian.dto.ErpShopDto;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class DmpOutputLxSellerShopRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{
	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpShopInfoEntity> dmpShopInfoEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_shop_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpShopInfoEntity dmpShopInfoEntity = (DmpShopInfoEntity) v;
						dmpShopInfoEntityMap.put(dmpShopInfoEntity.getId(), dmpShopInfoEntity);
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
				if("dmp_shop_info".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpShopInfoEntity dmpShopInfoEntity = dmpShopInfoEntityMap.get(changId);
			ErpShopDto shop = this.convert(dmpShopInfoEntity, cfgOutputId);
			if(shop != null) {
				map.put(dmpShopInfoEntity.getId(), JSON.toJSONString(shop));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public ErpShopDto convert(DmpShopInfoEntity dmpShopInfoEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpShopInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	ErpShopDto shop = new ErpShopDto();
    	
    	BeanUtils.copyProperties(dmpShopInfoEntity,shop);
    	
    	shop.setShopId(dmpShopInfoEntity.getThirdId());
        shop.setPlatformId(dmpShopInfoEntity.getPlatformId());
        shop.setSubPlatformId(dmpShopInfoEntity.getSubPlatformId());
        shop.setDisabled(dmpShopInfoEntity.getDisabled());
        shop.setSysType(PlatformDictEnum.LING_XING.getCode());
        shop.setAccountId(dmpShopInfoEntity.getAccountUserName());
        shop.setGroupId(dmpShopInfoEntity.getGroupId());
        shop.setCode(dmpShopInfoEntity.getBillCode());
        shop.setName(dmpShopInfoEntity.getName());
        shop.setContacts(dmpShopInfoEntity.getContacts());
        shop.setTelNumber(dmpShopInfoEntity.getTelNumber());
        shop.setAuthState(dmpShopInfoEntity.getAuthState());
        shop.setAuthTime(LocalDateUtil.formatTime(dmpShopInfoEntity.getAuthTime(), "yyyy-MM-dd HH:mm:ss"));
        shop.setCreated(LocalDateUtil.formatTime(dmpShopInfoEntity.getPlatformCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        shop.setModified(LocalDateUtil.formatTime(dmpShopInfoEntity.getPlatformUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
    	
        return shop;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("code");
    }
}