package com.erp.server.dmp.inout.handler.output.task.mq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.erp.model.dmp.entity.BiSkuInfoEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputKingdeeProductRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

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
				BiSkuInfoEntity biSkuInfoEntity = this.convert(dmpProductInfoEntity, dmpSkuInfoEntity, cfgOutputId);
				if(biSkuInfoEntity != null) {
					map.put(dmpSkuInfoEntity.getId(), JSON.toJSONString(biSkuInfoEntity));
				}
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public BiSkuInfoEntity convert(DmpProductInfoEntity dmpProductInfoEntity , DmpSkuInfoEntity dmpSkuInfoEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpProductInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	if(this.validateDataBlack(dmpSkuInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	BiSkuInfoEntity biSkuInfoEntity = new BiSkuInfoEntity();
    	
    	biSkuInfoEntity.setItemCode(dmpSkuInfoEntity.getSkuId());
        //sku编号
        biSkuInfoEntity.setSkuNo(dmpSkuInfoEntity.getSkuNo());
        //中文名
        biSkuInfoEntity.setNameCn(dmpSkuInfoEntity.getName());
        //英文名
        biSkuInfoEntity.setNameEn("");
        //统一成本价
        biSkuInfoEntity.setDefaultCost(dmpSkuInfoEntity.getDeclarePrice());
        //商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
        biSkuInfoEntity.setStatus(Integer.valueOf(dmpSkuInfoEntity.getStatus()));
        //商品创建时间
        biSkuInfoEntity.setSkuCreateTime(dmpProductInfoEntity.getPlatformCreateTime());
        //商品修改时间
        biSkuInfoEntity.setSkuUpdateTime(dmpProductInfoEntity.getPlatformUpdateTime());
        //品牌
        biSkuInfoEntity.setBrandName("");
        //商品目录(一级)
        biSkuInfoEntity.setParentCategoryName(dmpSkuInfoEntity.getParentCategoryName());
        //商品目录(二级)
        biSkuInfoEntity.setCategoryName(dmpSkuInfoEntity.getCategoryName());
        //售价
        biSkuInfoEntity.setSalePrice(dmpSkuInfoEntity.getSellPrice());
        //申报价格
        biSkuInfoEntity.setDeclarePrice(BigDecimal.ZERO);
        //开发员id
        biSkuInfoEntity.setDeveloperId("");
        //开发员名称
        biSkuInfoEntity.setDeveloperName("");
        //平台标识
        biSkuInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        //企业id
        biSkuInfoEntity.setCompanyId(dmpSkuInfoEntity.getCompanyId());
        //企业名称
        biSkuInfoEntity.setCompanyName(dmpSkuInfoEntity.getCompanyName());
        //上市时间
        biSkuInfoEntity.setListingTime(dmpSkuInfoEntity.getListingTime());
        
        //物料属性
        biSkuInfoEntity.setItemProperty(dmpSkuInfoEntity.getProdcutProperty());
        biSkuInfoEntity.setCreateTime(LocalDateTime.now());
    	
        return biSkuInfoEntity;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("skuNo");
    }
}
