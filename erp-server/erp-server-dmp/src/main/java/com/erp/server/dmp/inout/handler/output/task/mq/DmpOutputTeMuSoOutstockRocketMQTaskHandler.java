package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.TeMuSoOutStockDTO;
import com.common.business.dto.TeMuSoOutStockDetailDTO;
import com.common.business.dto.WdtSoOutStockDTO;
import com.common.business.dto.WdtSoOutStockDetailDTO;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoOutstockDetailEntity;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.dmp.kingdee.item.KingdeeDeliveryDetailItemEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class DmpOutputTeMuSoOutstockRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

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
		for(String changId : changeIds) {
			TeMuSoOutStockDTO teMuSoOutStockDTO = this.initOutStockInfoEntity(dmpSoOutstockEntityMap.get(changId));
            List<TeMuSoOutStockDetailDTO> initOrderItem = this.initOutStockDetailEntity(dmpSoOutstockEntityMap.get(changId), dmpSoOutstockDetailEntityEntityMap.get(changId));
            teMuSoOutStockDTO.setDetailList(initOrderItem);
            map.put(changId, JSON.toJSONString(teMuSoOutStockDTO));
        }
		return map;
	}

	private List<TeMuSoOutStockDetailDTO> initOutStockDetailEntity(DmpSoOutstockEntity dmpSoOutstockEntity, List<DmpSoOutstockDetailEntity> dmpSoOutstockDetailEntities) {
		List<TeMuSoOutStockDetailDTO> teMuSoOutStockDetailDTOS = new ArrayList<>();
		for (DmpSoOutstockDetailEntity dmpSoOutstockDetailEntity : dmpSoOutstockDetailEntities) {
			TeMuSoOutStockDetailDTO teMuSoOutStockDetailDTO = new TeMuSoOutStockDetailDTO();
			teMuSoOutStockDetailDTO.setPlatformWarehouseCode(dmpSoOutstockDetailEntity.getWarehouseId());
			teMuSoOutStockDetailDTO.setPlatformSkuNo(dmpSoOutstockDetailEntity.getPlatformSku());
			teMuSoOutStockDetailDTO.setQty(dmpSoOutstockDetailEntity.getQty());
			teMuSoOutStockDetailDTOS.add(teMuSoOutStockDetailDTO);
		}
		return teMuSoOutStockDetailDTOS;
	}

	private TeMuSoOutStockDTO initOutStockInfoEntity(DmpSoOutstockEntity dmpSoOutstockEntity) {
		TeMuSoOutStockDTO teMuSoOutStockDTO = new TeMuSoOutStockDTO();
		teMuSoOutStockDTO.setOutTime(dmpSoOutstockEntity.getDeliveryTime());
		teMuSoOutStockDTO.setShopId(dmpSoOutstockEntity.getShopId());
		teMuSoOutStockDTO.setPlatformOrderCode(dmpSoOutstockEntity.getPlatformCode());
		teMuSoOutStockDTO.setTransportNo(dmpSoOutstockEntity.getLogisticsCode());
		return teMuSoOutStockDTO;
	}


	@Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("platformOrderCode");
    }
}
