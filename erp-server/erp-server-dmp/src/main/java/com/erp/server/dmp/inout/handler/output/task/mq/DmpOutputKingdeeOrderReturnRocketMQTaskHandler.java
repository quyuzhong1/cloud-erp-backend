package com.erp.server.dmp.inout.handler.output.task.mq;

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
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.dmp.kingdee.item.KingdeeReturnOrderItemEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputKingdeeOrderReturnRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpSoReturnInfoEntity> dmpSoReturnInfoEntityMap = new HashMap<>();
		Map<String, List<DmpSoReturnDetailEntity>> dmpSoReturnDetailEntityEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_so_return_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoReturnInfoEntity dmpSoReturnInfoEntity = (DmpSoReturnInfoEntity) v;
						dmpSoReturnInfoEntityMap.put(dmpSoReturnInfoEntity.getId(), dmpSoReturnInfoEntity);
					}
				}else if("dmp_so_return_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoReturnDetailEntity dmpSoReturnDetailEntity = (DmpSoReturnDetailEntity) v;
						String mainId = dmpSoReturnDetailEntity.getMainId();
						List<DmpSoReturnDetailEntity> list = dmpSoReturnDetailEntityEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoReturnDetailEntity);
						dmpSoReturnDetailEntityEntityMap.put(mainId, list);
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
				if("dmp_so_return_info".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}else if("dmp_so_return_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoReturnDetailEntity dmpSoReturnDetailEntity = (DmpSoReturnDetailEntity) v;
						changeIds.add(dmpSoReturnDetailEntity.getMainId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			KingdeeReturnOrderEntity kingdeeReturnOrderEntity = this.initOrderInfoEntity(dmpSoReturnInfoEntityMap.get(changId) , cfgOutputId);
			if(kingdeeReturnOrderEntity != null) {
				kingdeeReturnOrderEntity.setItemEntityList(this.initOrderItem(dmpSoReturnDetailEntityEntityMap.get(changId)));
				map.put(changId, JSON.toJSONString(kingdeeReturnOrderEntity));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public KingdeeReturnOrderEntity initOrderInfoEntity(DmpSoReturnInfoEntity entity , String cfgOutputId) {
    	if(this.validateDataBlack(entity, cfgOutputId)) {
    		return null;
    	}
    	KingdeeReturnOrderEntity resultEntity = new KingdeeReturnOrderEntity();
    	resultEntity.setFSaleOrgId(entity.getSaleOrgId());
    	resultEntity.setFULZDataSources(entity.getUlzDataSources());
    	resultEntity.setFRetcustNumber(entity.getShopId());
    	resultEntity.setFRetcustName(entity.getShopName());
    	resultEntity.setFBillNo(entity.getThirdCode());
    	resultEntity.setFSaleOrgName(entity.getSaleOrgName());
    	resultEntity.setFSaledeptNumber(entity.getSaledeptNumber());
    	resultEntity.setFSaledeptName(entity.getSaledeptName());
    	resultEntity.setFDate(LocalDateUtil.formatTime(entity.getBillDate(), "yyyy-MM-dd'T'HH:mm:ss.SSS"));
    	resultEntity.setFEThirdBillNo(entity.getThirdBillNo());
    	resultEntity.setFDocumentStatus(entity.getStatus());
        return resultEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public List<KingdeeReturnOrderItemEntity> initOrderItem(List<DmpSoReturnDetailEntity> itemList) {
        List<KingdeeReturnOrderItemEntity> orderItemList = new ArrayList<>();
        for (DmpSoReturnDetailEntity item : itemList) {
        	KingdeeReturnOrderItemEntity itemEntity = new KingdeeReturnOrderItemEntity();
        	itemEntity.setFStockNumber(item.getWarehouseId());
        	itemEntity.setFMaterialNumber(item.getSkuNo());
        	itemEntity.setFOrderNo(item.getThirdOrderCode());
        	itemEntity.setFRealQty(item.getQty().toString());
        	itemEntity.setFStockLocId(item.getWarehouseLocation());
        	itemEntity.setFSOEntryId(item.getSoEntryId());
            orderItemList.add(itemEntity);
        }
        return orderItemList;
    }
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("FBillNo");
    }
}
