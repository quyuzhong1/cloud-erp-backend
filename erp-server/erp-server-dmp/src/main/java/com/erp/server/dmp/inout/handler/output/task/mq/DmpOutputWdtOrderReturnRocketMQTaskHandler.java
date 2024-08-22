package com.erp.server.dmp.inout.handler.output.task.mq;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.WdtReturnOrderDTO;
import com.common.business.dto.WdtReturnOrderDetailDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputWdtOrderReturnRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

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
			List<DmpSoReturnDetailEntity> itemList = dmpSoReturnDetailEntityEntityMap.get(changId);
			WdtReturnOrderDTO wdtReturnOrderDTO = this.initOrderInfoEntity(dmpSoReturnInfoEntityMap.get(changId) , itemList , cfgOutputId);
			if(wdtReturnOrderDTO != null) {
				wdtReturnOrderDTO.setDetailList(this.initOrderItem(itemList));
				map.put(changId, JSON.toJSONString(wdtReturnOrderDTO));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public WdtReturnOrderDTO initOrderInfoEntity(DmpSoReturnInfoEntity entity , List<DmpSoReturnDetailEntity> itemList , String cfgOutputId) {
    	if(this.validateDataBlack(entity, cfgOutputId)) {
    		return null;
    	}
    	WdtReturnOrderDTO resultEntity = new WdtReturnOrderDTO();
    	
    	resultEntity.setThirdCode(entity.getThirdCode());
        resultEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getCode());
        resultEntity.setType(OrderTypeEnum.B2C.getCode());
        LocalDateTime approveTime = entity.getReturnTime();
        resultEntity.setBillDate(approveTime.toLocalDate());
        resultEntity.setInvalidStatus(false);
        resultEntity.setApproveUserName("wangdiantong");
        resultEntity.setCreateUserName("wangdiantong");
        resultEntity.setApproveTime(approveTime);
        //店铺id
        resultEntity.setShopId(entity.getShopId());
        resultEntity.setShopName(entity.getShopName());
        resultEntity.setCreated(entity.getPlatformCreateTime());
        resultEntity.setModified(entity.getPlatformUpdateTime());
        //仓库id
        resultEntity.setWarehouseId(itemList.stream().map(DmpSoReturnDetailEntity::getWarehouseId).filter(StringUtils::isNotBlank).findAny().orElse(""));
        resultEntity.setWarehouseName(itemList.stream().map(DmpSoReturnDetailEntity::getWarehouseName).filter(StringUtils::isNotBlank).findAny().orElse(""));
        resultEntity.setSourceType(SourceTypeEnum.WDT_RETURN_ORDER.getCode());
        resultEntity.setSourceId(itemList.stream().map(DmpSoReturnDetailEntity::getPlatformOrderCode).distinct().collect(Collectors.joining(",")));
        resultEntity.setSourceCode(itemList.stream().map(DmpSoReturnDetailEntity::getThirdOrderCode).distinct().collect(Collectors.joining(",")));
    	
        return resultEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public List<WdtReturnOrderDetailDTO> initOrderItem(List<DmpSoReturnDetailEntity> itemList) {
        List<WdtReturnOrderDetailDTO> orderItemList = new ArrayList<>();
        for (DmpSoReturnDetailEntity item : itemList) {
        	WdtReturnOrderDetailDTO itemEntity = new WdtReturnOrderDetailDTO();
        	
        	itemEntity.setSkuNo(item.getSkuNo());
//        	itemEntity.setMustQty(item.get);
        	itemEntity.setReceiveQty(item.getQty());
            itemEntity.setRealQty(item.getQty());
            itemEntity.setReturnReasonDict(item.getReason());
            itemEntity.setWarehouseLocation(item.getWarehouseLocation());
            itemEntity.setIsSubContract(false);
            itemEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getCode());
            itemEntity.setWarehouseId(item.getWarehouseId());
            itemEntity.setSoReturnDetailId(item.getPlatformDetailId());
            itemEntity.setSourceDetailId(item.getThirdDetailId());
            itemEntity.setAmount(item.getAmount());
        	
            orderItemList.add(itemEntity);
        }
        return orderItemList;
    }
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("thirdCode");
    }
}
