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
import com.erp.model.dmp.entity.DmpSoOutstockDetailEntity;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputKingdeeSoOutstockRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpSoOutstockEntity> dmpSoOutstockEntityMap = new HashMap<>();
		Map<String, List<DmpSoOutstockDetailEntity>> dmpSoOutstockDetailEntityEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_so_outstock".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockEntity dmpSoOutstockEntity = (DmpSoOutstockEntity) v;
						dmpSoOutstockEntityMap.put(dmpSoOutstockEntity.getId(), dmpSoOutstockEntity);
					}
				}else if("dmp_so_outstock_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockDetailEntity dmpSoOutstockDetailEntity = (DmpSoOutstockDetailEntity) v;
						String mainId = dmpSoOutstockDetailEntity.getMainId();
						List<DmpSoOutstockDetailEntity> list = dmpSoOutstockDetailEntityEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoOutstockDetailEntity);
						dmpSoOutstockDetailEntityEntityMap.put(mainId, list);
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
				if("dmp_so_outstock".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}else if("dmp_so_outstock_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockDetailEntity dmpSoOutstockDetailEntity = (DmpSoOutstockDetailEntity) v;
						changeIds.add(dmpSoOutstockDetailEntity.getMainId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			KingdeeDeliveryDetailEntity kingdeeReturnOrderEntity = this.initOrderInfoEntity(dmpSoOutstockEntityMap.get(changId) , cfgOutputId);
			if(kingdeeReturnOrderEntity != null) {
				kingdeeReturnOrderEntity.setKingdeeOutStockItemEntityList(this.initOrderItem(dmpSoOutstockDetailEntityEntityMap.get(changId)));
				map.put(changId, JSON.toJSONString(kingdeeReturnOrderEntity));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public KingdeeDeliveryDetailEntity initOrderInfoEntity(DmpSoOutstockEntity entity , String cfgOutputId) {
    	if(this.validateDataBlack(entity, cfgOutputId)) {
    		return null;
    	}
    	KingdeeDeliveryDetailEntity resultEntity = new KingdeeDeliveryDetailEntity();
    	resultEntity.setFCustomerNumber(entity.getShopId());
    	resultEntity.setFCustomerName(entity.getShopName());
    	resultEntity.setFBillNo(entity.getThirdCode());
    	resultEntity.setFCarriageNO(entity.getLogisticsCode());
    	resultEntity.setFEThirdBillNo(entity.getThirdBillNo());
    	resultEntity.setFStockerName(entity.getStockerName());
    	resultEntity.setFDocumentStatus(entity.getStatus());
    	resultEntity.setFDate(LocalDateUtil.formatTime(entity.getBillDate(), "yyyy-MM-dd'T'HH:mm:ss"));
    	resultEntity.setIsNew(Boolean.TRUE);
        return resultEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public List<KingdeeDeliveryDetailItemEntity> initOrderItem(List<DmpSoOutstockDetailEntity> itemList) {
        List<KingdeeDeliveryDetailItemEntity> orderItemList = new ArrayList<>();
        for (DmpSoOutstockDetailEntity item : itemList) {
        	KingdeeDeliveryDetailItemEntity itemEntity = new KingdeeDeliveryDetailItemEntity();
        	itemEntity.setFStockNumber(item.getWarehouseId());
        	itemEntity.setFMaterialNumber(item.getSkuNo());
        	itemEntity.setFRealQty(item.getQty().toString());
        	itemEntity.setFNote(item.getRemark());
        	
            orderItemList.add(itemEntity);
        }
        return orderItemList;
    }
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("fBillNo");
    }
}
