package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.oms.enums.ListingInfoPlatformStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class MercadoLocalProductRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

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
			if(CollUtil.isEmpty(dmpSkuInfoEntityList)) {
				continue;
			}
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
    	if(dmpProductInfoEntity == null || dmpSkuInfoEntity == null) {
    		return null;
    	}
    	if(this.validateDataBlack(dmpSkuInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	PlatformProductDTO product = new PlatformProductDTO();
    	
    	product.setPlatform(PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode());
        // 平台sku no
        product.setPlatformProductNo(dmpSkuInfoEntity.getSpuId());
        // 平台sku 名
        String spuName = dmpProductInfoEntity.getSpuName();
		product.setPlatformProductName(spuName);
        String skuNo = dmpSkuInfoEntity.getSkuNo();
		product.setPlatformSkuNo(StringUtils.isBlank(skuNo)? "" : skuNo);

        product.setPlatformSkuName(spuName);
        // 类型 platform 平台  warehouse 仓库
        product.setPlatformType("platform");
        String imageUrls = dmpSkuInfoEntity.getImageUrls();
        if (StringUtils.isBlank(imageUrls)) {
            product.setProductImageUrl("");
        } else {
            String imageUrl = imageUrls.split(";")[0];
            product.setProductImageUrl(imageUrl);
        }
        product.setShopId(dmpProductInfoEntity.getNextLevelId());
        // 包装信息
        String packing = StrUtil.format("长度:{}{};宽度:{}{};高度:{}{};重量:{}{};",
				dmpSkuInfoEntity.getPackageLength(),dmpSkuInfoEntity.getPackageUnit(),
				dmpSkuInfoEntity.getPackageWidth(),dmpSkuInfoEntity.getPackageUnit(),
				dmpSkuInfoEntity.getPackageHeight(),dmpSkuInfoEntity.getPackageUnit(),
				dmpSkuInfoEntity.getGrossWeight(),dmpSkuInfoEntity.getWeightUnit());
        product.setProductPacking(packing);
        product.setPlatformUpdateTime(dmpSkuInfoEntity.getPlatformUpdateTime());
        product.setPlatformSkuId(dmpSkuInfoEntity.getSkuId());

        // 平台唯一标识=平台skuId + 店铺ID
        String uniqueId = StrUtil.format("{}_{}", dmpSkuInfoEntity.getSkuId(), dmpProductInfoEntity.getNextLevelId());
        product.setUniqueId(uniqueId);

		//父平台产品ID（父ASIN）
		product.setPlatformParentSpuNo(dmpSkuInfoEntity.getPlatformParentSpuNo());
		//平台的Listing状态
		if (StringUtils.isNotBlank(dmpSkuInfoEntity.getStatus())) {
			String status = dmpSkuInfoEntity.getStatus();
			if ("1".equalsIgnoreCase(status)) {
				status = ListingInfoPlatformStatusEnum.ACTIVE.getCode();
			} else {
				status = ListingInfoPlatformStatusEnum.INACTIVE.getCode();
			}
			product.setPlatformStatus(status);
		}
        return product;
    }
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("platformSkuNo");
    }

}
