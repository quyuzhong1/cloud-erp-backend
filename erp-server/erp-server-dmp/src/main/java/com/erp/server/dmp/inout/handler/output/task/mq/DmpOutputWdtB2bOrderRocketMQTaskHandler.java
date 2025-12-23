package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformB2bOrderDTO;
import com.common.business.dto.PlatformB2bOrderDetailDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.ThirdMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputWdtB2bOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Resource
	private ThirdMappingService thirdMappingService;

	@Resource
	private ShopInfoFeign shopInfoFeign;

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String, DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
		Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
		for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if (CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if ("dmp_so_info".equals(storageName)) {
					for (BaseEntity v : value) {
						DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
						dmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
					}
				} else if ("dmp_so_detail".equals(storageName)) {
					for (BaseEntity v : value) {
						DmpSoDetailEntity dmpSoDetailEntity = (DmpSoDetailEntity) v;
						String mainId = dmpSoDetailEntity.getMainId();
						List<DmpSoDetailEntity> list = dmpSoDetailEntityMap.get(mainId);
						if (CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoDetailEntity);
						dmpSoDetailEntityMap.put(mainId, list);
					}
				}
			}
		}

		Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
		Set<String> changeIds = new HashSet<>();
		for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
			if (CollUtil.isNotEmpty(value)) {
				String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
				if ("dmp_so_info".equals(storageName)) {
					for (BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				} else if ("dmp_so_detail".equals(storageName)) {
					for (BaseEntity v : value) {
						DmpSoDetailEntity dmpSoReturnDetailEntity = (DmpSoDetailEntity) v;
						changeIds.add(dmpSoReturnDetailEntity.getMainId());
					}
				}
			}
		}

		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			PlatformB2bOrderDTO orderDTO = this.convert(dmpSoInfoEntityMap.get(changId), dmpSoDetailEntityMap.get(changId), cfgOutputId);
			if(orderDTO != null) {
				map.put(changId, JSON.toJSONString(orderDTO));
			}
		}
		return map;
	}

	/**
	 * 解析订单数据
	 **/
	public PlatformB2bOrderDTO convert(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> dmpSoDetailEntityList , String cfgOutputId) {
		if (this.validateDataBlack(dmpSoInfoEntity, cfgOutputId)) {
			return null;
		}
		if (CollUtil.isEmpty(dmpSoDetailEntityList)) {
			return null;
		}
		//查询店铺
		String platformShopId = dmpSoInfoEntity.getShopId();
		ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
		viewParamDTO.setThirdCode(platformShopId);
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

		PlatformB2bOrderDTO platformB2bOrderDTO = new PlatformB2bOrderDTO();
		platformB2bOrderDTO.setThirdSystem(PlatformDictEnum.WDT.getCode());
		platformB2bOrderDTO.setBillDate(dmpSoInfoEntity.getPlatformCreateTime().toLocalDate());
		platformB2bOrderDTO.setShippingFee(dmpSoInfoEntity.getShippingAmount());
		platformB2bOrderDTO.setOrderAmount(dmpSoInfoEntity.getAllAmount());
		platformB2bOrderDTO.setDiscountAmount(dmpSoInfoEntity.getTotalDiscount());
		platformB2bOrderDTO.setIsCollectShippingFee(Objects.nonNull(dmpSoInfoEntity.getShippingAmount()) && dmpSoInfoEntity.getShippingAmount().compareTo(BigDecimal.ZERO) > 0);
		platformB2bOrderDTO.setPlatformOrderCode(dmpSoInfoEntity.getPlatformCode());
		platformB2bOrderDTO.setCode(dmpSoInfoEntity.getPlatformCode());
		platformB2bOrderDTO.setThirdCode(dmpSoInfoEntity.getThirdCode());
		platformB2bOrderDTO.setPlatformShopId(dmpSoInfoEntity.getShopId());
		platformB2bOrderDTO.setShopId(shopInfo.getId());
		platformB2bOrderDTO.setCustomerOrderNo(dmpSoInfoEntity.getPlatformCode());

		String orderStatus = dmpSoInfoEntity.getOrderStatus();
		if("55".equals(orderStatus)){
			platformB2bOrderDTO.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
			platformB2bOrderDTO.setIsInvalid(false);
		}else if("10".equals(orderStatus)){
			platformB2bOrderDTO.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
			platformB2bOrderDTO.setIsInvalid(true);
		}else{
			return null;
		}


		List<PlatformB2bOrderDetailDTO> details = new ArrayList<>();

		//订单详情
		for (int i = 0; i < dmpSoDetailEntityList.size(); i++) {
			DmpSoDetailEntity dmpSoDetailEntity = dmpSoDetailEntityList.get(i);
			PlatformB2bOrderDetailDTO detailDTO = new PlatformB2bOrderDetailDTO();
			detailDTO.setSkuNo(dmpSoDetailEntity.getSkuNo());
			detailDTO.setPlatformSkuNo(dmpSoDetailEntity.getSkuId());
			detailDTO.setCustomerSkuNo(dmpSoDetailEntity.getSkuId());
			detailDTO.setQty(dmpSoDetailEntity.getQty());
			detailDTO.setTaxRate(dmpSoInfoEntity.getTaxRate());
			detailDTO.setTaxPrice(dmpSoDetailEntity.getSellPriceOrigin());

			details.add(detailDTO);
		}
		platformB2bOrderDTO.setDetail(details);
		return platformB2bOrderDTO;
	}

	@Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("code");
    }
}
