package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformB2BReturnOrderDTO;
import com.common.business.dto.WdtReturnOrderDTO;
import com.common.business.dto.WdtReturnOrderDetailDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.ThirdMappingService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class DmpOutputWdtB2BReturnRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Resource
	private ThirdMappingService thirdMappingService;

	@Resource
	private ShopInfoFeign shopInfoFeign;

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
			PlatformB2BReturnOrderDTO wdtReturnOrderDTO = this.convert(dmpSoReturnInfoEntityMap.get(changId) , itemList , cfgOutputId);
			if(wdtReturnOrderDTO != null) {
				map.put(changId, JSON.toJSONString(wdtReturnOrderDTO));
			}
		}
		return map;
	}

	private PlatformB2BReturnOrderDTO convert(DmpSoReturnInfoEntity dmpSoReturnInfoEntity, List<DmpSoReturnDetailEntity> itemList, String cfgOutputId) {
		if (this.validateDataBlack(dmpSoReturnInfoEntity, cfgOutputId)) {
			return null;
		}
		if (CollUtil.isEmpty(itemList)) {
			return null;
		}
		if(Objects.isNull(dmpSoReturnInfoEntity.getReturnTime())){
			return null;
		}
		//查询店铺
		String platformShopId = dmpSoReturnInfoEntity.getShopId();
		ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
		viewParamDTO.setThirdId(platformShopId);
		viewParamDTO.setType(ThirdSysTypeEnum.SHOP.getCode());
		viewParamDTO.setSysType(PlatformDictEnum.WDT.getCode());
		List<ThirdMappingEntity> thirdMappingEntityList = thirdMappingService.getByThirdId(viewParamDTO);
		if(CollectionUtils.isEmpty(thirdMappingEntityList)){
			return null;
		}
		ThirdMappingEntity thirdMappingEntity = thirdMappingEntityList.get(0);
		ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(thirdMappingEntity.getSysId());
		if(Objects.isNull(shopInfo)){
			return null;
		}
		if(!shopInfo.getAuthStatus().equals(AuthStatusEnum.ALREADY.getCode())){
			return null;
		}
		//只推送京东自营的店铺
		if(!PlatformDictEnum.SOP.getCode().equals(shopInfo.getDictPlatform())){
			return null;
		}
		PlatformB2BReturnOrderDTO platformB2BReturnOrderDTO = new PlatformB2BReturnOrderDTO();
		platformB2BReturnOrderDTO.setThirdCode(dmpSoReturnInfoEntity.getThirdCode());
		platformB2BReturnOrderDTO.setPlatformOrderCode(dmpSoReturnInfoEntity.getPlatformCode());
		platformB2BReturnOrderDTO.setShopId(shopInfo.getId());
		platformB2BReturnOrderDTO.setPlatformWarehouseId(itemList.get(0).getWarehouseId());
		platformB2BReturnOrderDTO.setReturnLogisticCode(dmpSoReturnInfoEntity.getTrackingNumber());
		platformB2BReturnOrderDTO.setPlatformOrderType(dmpSoReturnInfoEntity.getPlatformOrderType());
		if(Objects.nonNull(dmpSoReturnInfoEntity.getReturnTime())){
			platformB2BReturnOrderDTO.setBillDate(dmpSoReturnInfoEntity.getReturnTime().toLocalDate());
		}
		if("10".equals(dmpSoReturnInfoEntity.getStatus())){
			platformB2BReturnOrderDTO.setInvalidStatus(true);
		}
		List<PlatformB2BReturnOrderDTO.Detail> detailList = new ArrayList<>();
		for (DmpSoReturnDetailEntity dmpSoReturnDetailEntity : itemList) {
			PlatformB2BReturnOrderDTO.Detail detail = new PlatformB2BReturnOrderDTO.Detail();
			detail.setSkuNo(dmpSoReturnDetailEntity.getSkuNo());
			detail.setPlatformSkuNo(dmpSoReturnDetailEntity.getSkuNo());
			detail.setReturnQty(dmpSoReturnDetailEntity.getQty());
			detail.setReturnAmount(dmpSoReturnDetailEntity.getAmount());
			detail.setReturnReasonDict(dmpSoReturnInfoEntity.getRemarkName());
			if("2".equals(dmpSoReturnInfoEntity.getPlatformOrderType())){
				detail.setReturnTypeDict(ReturnTypeEnum.DEDUCTION.getCode());
			}else if("3".equals(dmpSoReturnInfoEntity.getPlatformOrderType())){
				detail.setReturnTypeDict(ReturnTypeEnum.REPLENISHMENT.getCode());
			}
			detail.setRemark(dmpSoReturnInfoEntity.getRemark());
			detailList.add(detail);
		}
		platformB2BReturnOrderDTO.setDetailList(detailList);
		return platformB2BReturnOrderDTO;
	}


	@Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("thirdCode");
    }
}
