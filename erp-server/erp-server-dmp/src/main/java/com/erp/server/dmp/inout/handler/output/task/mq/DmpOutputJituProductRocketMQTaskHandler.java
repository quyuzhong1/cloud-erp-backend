package com.erp.server.dmp.inout.handler.output.task.mq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformProductDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputJituProductRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpProductInfoEntity> dmpProductInfoEntityMap = new HashMap<>();
		Map<String, List<DmpSkuInfoEntity>> dmpSkuInfoEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_product_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpProductInfoEntity dmpProductInfoEntity = (DmpProductInfoEntity) v;
						dmpProductInfoEntityMap.put(dmpProductInfoEntity.getId(), dmpProductInfoEntity);
					}
				}else if("dmp_sku_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSkuInfoEntity dmpSoReturnDetailEntity = (DmpSkuInfoEntity) v;
						String mainId = dmpSoReturnDetailEntity.getMainId();
						List<DmpSkuInfoEntity> list = dmpSkuInfoEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoReturnDetailEntity);
						dmpSkuInfoEntityMap.put(mainId, list);
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
				if("dmp_product_info".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}else if("dmp_sku_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSkuInfoEntity dmpSoReturnDetailEntity = (DmpSkuInfoEntity) v;
						changeIds.add(dmpSoReturnDetailEntity.getMainId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpProductInfoEntity dmpProductInfoEntity = dmpProductInfoEntityMap.get(changId);
			List<DmpSkuInfoEntity> dmpSkuInfoEntityList = dmpSkuInfoEntityMap.get(changId);
			for(DmpSkuInfoEntity dmpSkuInfoEntity : dmpSkuInfoEntityList) {
				PlatformProductDTO product = this.convert(dmpProductInfoEntity, dmpSkuInfoEntity, cfgOutputId);
				if(product != null) {
					map.put(dmpSkuInfoEntity.getId(), JSON.toJSONString(product));
				}
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformProductDTO convert(DmpProductInfoEntity dmpProductInfoEntity , DmpSkuInfoEntity dmpSkuInfoEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpSkuInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	PlatformProductDTO product = new PlatformProductDTO();
    	
    	product.setPlatformType("warehouse");
    	product.setType(RuleTypeEnum.WAREHOUSE.getCode());
    	String sourcePlatform = dmpProductInfoEntity.getSourcePlatform();
    	product.setPlatform(sourcePlatform);
    	product.setAuthId(dmpProductInfoEntity.getAuthId());
    	
    	String skuNo = dmpSkuInfoEntity.getSkuNo();
		product.setPlatformSkuNo(skuNo);
    	product.setPlatformSkuName(dmpSkuInfoEntity.getName());
    	
    	String brandName = dmpSkuInfoEntity.getBrandName();
    	if(StringUtils.isBlank(brandName)) {
    		brandName = skuNo;
    	}
    	product.setPlatformProductNo(brandName);
		product.setPlatformProductBarcode(brandName);
        return product;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("platformSkuNo");
    }
}
