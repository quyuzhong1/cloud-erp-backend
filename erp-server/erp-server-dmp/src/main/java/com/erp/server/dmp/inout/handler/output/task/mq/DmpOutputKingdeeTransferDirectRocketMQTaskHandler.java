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
import com.erp.model.dmp.dto.DmpTransferInfoDTO;
import com.erp.model.dmp.dto.DmpTransferInfoDetailDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpDirectTransferDetailEntity;
import com.erp.model.dmp.entity.DmpDirectTransferEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputKingdeeTransferDirectRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpDirectTransferEntity> dmpDirectTransferEntityMap = new HashMap<>();
		Map<String, List<DmpDirectTransferDetailEntity>> dmpDirectTransferDetailEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_direct_transfer".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpDirectTransferEntity dmpDirectTransferEntity = (DmpDirectTransferEntity) v;
						dmpDirectTransferEntityMap.put(dmpDirectTransferEntity.getId(), dmpDirectTransferEntity);
					}
				}else if("dmp_direct_transfer_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpDirectTransferDetailEntity dmpDirectTransferDetailEntity = (DmpDirectTransferDetailEntity) v;
						String mainId = dmpDirectTransferDetailEntity.getMainId();
						List<DmpDirectTransferDetailEntity> list = dmpDirectTransferDetailEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpDirectTransferDetailEntity);
						dmpDirectTransferDetailEntityMap.put(mainId, list);
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
				if("dmp_direct_transfer".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}else if("dmp_direct_transfer_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpDirectTransferDetailEntity dmpDirectTransferDetailEntity = (DmpDirectTransferDetailEntity) v;
						changeIds.add(dmpDirectTransferDetailEntity.getMainId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpTransferInfoDTO dmpTransferInfoDTO = this.initOrderInfoEntity(dmpDirectTransferEntityMap.get(changId) , cfgOutputId);
			if(dmpTransferInfoDTO != null) {
				dmpTransferInfoDTO.setDetailList(this.initOrderItem(dmpDirectTransferDetailEntityMap.get(changId)));
				map.put(changId, JSON.toJSONString(dmpTransferInfoDTO));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public DmpTransferInfoDTO initOrderInfoEntity(DmpDirectTransferEntity entity , String cfgOutputId) {
        // 过滤小隼和优至胜的订单
    	if(this.validateDataBlack(entity, cfgOutputId)) {
    		return null;
    	}
        //此处不跳过订单，避免订单修改仓库编码后，数据无法同步
        DmpTransferInfoDTO resultEntity = new DmpTransferInfoDTO();
        resultEntity.setCode(entity.getThirdCode());
        resultEntity.setType(entity.getBizType());
        resultEntity.setTransferType(entity.getTransferType());
        resultEntity.setTransferTypeCode(entity.getTransferType());
        resultEntity.setInOrgId(entity.getInOrgId());
        resultEntity.setInOrgCode(entity.getInOrgCode());
        resultEntity.setInOrgName(entity.getInOrgName());
        resultEntity.setOutOrgId(entity.getOutOrgId());
        resultEntity.setOutOrgCode(entity.getOutOrgCode());
        resultEntity.setOutOrgName(entity.getOutOrgName());
        resultEntity.setBillDate(entity.getBillDate().atStartOfDay());
        resultEntity.setApproveStatus("C");
        resultEntity.setTransferDirection(entity.getTransferDirect());
        resultEntity.setPlatformCreateUserName(entity.getPlatformCreateUserName());
        resultEntity.setPlatformCreateTime(entity.getPlatformCreateTime());
        resultEntity.setApproveUserName(entity.getApproveUserName());
        resultEntity.setApproveTime(entity.getApproveTime());
        resultEntity.setInvalidStatus(entity.getInvalidStatus());
        resultEntity.setInvalidTime(entity.getInvalidTime());
        resultEntity.setInvalidUserName(entity.getInvalidUserName());
        resultEntity.setRemark(entity.getRemark());
        resultEntity.setLastUpdatedTime(entity.getLastUpdatedTime());
        resultEntity.setLastUpdatedUserName(entity.getLastUpdatedUserName());
        resultEntity.setSourceId(entity.getId());
        resultEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        return resultEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public List<DmpTransferInfoDetailDTO> initOrderItem(List<DmpDirectTransferDetailEntity> itemList) {
        List<DmpTransferInfoDetailDTO> orderItemList = new ArrayList<>();
        for (DmpDirectTransferDetailEntity item : itemList) {
            DmpTransferInfoDetailDTO itemEntity = new DmpTransferInfoDetailDTO();
            itemEntity.setSkuNo(item.getSkuNo());
            itemEntity.setProductName(item.getProductName());
            itemEntity.setUnit(item.getProductUnit());
            itemEntity.setQty(item.getQty());
            itemEntity.setSourceDetailId(item.getThirdDetailId());
            itemEntity.setReceiveTime(item.getReceiveTime());
            itemEntity.setInStockStatusCode(item.getInStockStatusCode());
            itemEntity.setInStockStatusName(item.getInStockStatusName());
            itemEntity.setOutStockStatusCode(item.getOutStockStatusCode());
            itemEntity.setOutStockStatusName(item.getOutStockStatusName());
            itemEntity.setInWarehouseCode(item.getInWarehouseCode());
            itemEntity.setInWarehouseName(item.getInWarehouseName());
            itemEntity.setOutWarehouseCode(item.getOutWarehouseCode());
            itemEntity.setOutWarehouseName(item.getOutWarehouseName());
            itemEntity.setInWarehouseLocation(item.getInWarehouseLocation());
            itemEntity.setOutWarehouseLocation(item.getOutWarehouseLocation());
            orderItemList.add(itemEntity);
        }
        return orderItemList;
    }
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("code");
    }
}
