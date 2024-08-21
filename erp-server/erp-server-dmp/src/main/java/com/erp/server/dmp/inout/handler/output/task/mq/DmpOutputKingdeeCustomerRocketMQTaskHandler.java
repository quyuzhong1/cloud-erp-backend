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
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

@Service
@Scope("prototype")
public class DmpOutputKingdeeCustomerRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{
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
			BiShopInfoEntity dto = this.convert(dmpShopInfoEntity, cfgOutputId);
			if(dto != null) {
				map.put(dmpShopInfoEntity.getId(), JSON.toJSONString(dto));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public BiShopInfoEntity convert(DmpShopInfoEntity dmpShopInfoEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpShopInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	BiShopInfoEntity biShopInfoEntity = new BiShopInfoEntity();
    	
        //平台店铺编号
        biShopInfoEntity.setPlatformShopNo(dmpShopInfoEntity.getBillCode());
        //平台店铺账户
        String name = dmpShopInfoEntity.getName();
		biShopInfoEntity.setAccountUserName(name);
        //平台店铺标识
        biShopInfoEntity.setAccountStoreName(name);
        //店铺名称
        biShopInfoEntity.setName(name);
        //平台名称
        biShopInfoEntity.setPlatformName(dmpShopInfoEntity.getPlatformName());
        String orgName = dmpShopInfoEntity.getCompanyName();
        if(StrUtil.isNotBlank(orgName)){
            biShopInfoEntity.setIsVijim(Boolean.TRUE);
            if (orgName.contains("优至胜") || orgName.contains("小隼")) {
                biShopInfoEntity.setIsVijim(Boolean.FALSE);
            }
        }
        biShopInfoEntity.setUseOrgId(Integer.parseInt(dmpShopInfoEntity.getCompanyId()));
        biShopInfoEntity.setUseOrgName(orgName);
        //平台标识
        biShopInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        biShopInfoEntity.setCountry(dmpShopInfoEntity.getSite());
        biShopInfoEntity.setCustomerId(dmpShopInfoEntity.getGroupId());
        biShopInfoEntity.setCreateTime(LocalDateTime.now());
    	
        return biShopInfoEntity;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("sourceId");
    }
}
