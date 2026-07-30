package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformB2bOrderDTO;
import com.common.business.dto.PlatformB2bOrderDetailDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.ThirdMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputWdtB2bOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	/**
	 * 同一次推送任务内缓存字典默认税率，避免按单重复 Feign 查字典。
	 */
	private BigDecimal cachedDefaultTaxRate;

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
		platformB2bOrderDTO.setPlatformWarehouseId(dmpSoInfoEntity.getWarehouseId());
		// 京东自营 B2B 按含税处理，税率缺省时使用可配置默认值
		platformB2bOrderDTO.setIsTax(true);

		String orderStatus = dmpSoInfoEntity.getOrderStatus();
		platformB2bOrderDTO.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
		if("5".equals(orderStatus)){
			platformB2bOrderDTO.setIsInvalid(true);
		}else{
			platformB2bOrderDTO.setIsInvalid(false);
		}
		Map<String,List<DmpSoDetailEntity>> combineDetailMap = dmpSoDetailEntityList.stream().filter(v-> StringUtils.isNotBlank(v.getSuiteNo())).collect(Collectors.groupingBy(this::getCombineDetailGroupKey));
		List<PlatformB2bOrderDetailDTO> details = new ArrayList<>();
		BigDecimal taxRate = resolveWdtJdB2bTaxRate(dmpSoInfoEntity.getTaxRate());

		//订单详情 ERP-15125 如果是组合品，推送组合品明细
		for (int i = 0; i < dmpSoDetailEntityList.size(); i++) {
			DmpSoDetailEntity dmpSoDetailEntity = dmpSoDetailEntityList.get(i);
			if(StringUtils.isNotBlank(dmpSoDetailEntity.getSuiteNo())){
				continue;
			}
			PlatformB2bOrderDetailDTO detailDTO = new PlatformB2bOrderDetailDTO();
			detailDTO.setSkuNo(dmpSoDetailEntity.getSkuNo());
			detailDTO.setPlatformSkuNo(dmpSoDetailEntity.getPlatformSpuNo());
			detailDTO.setCustomerSkuNo(dmpSoDetailEntity.getPlatformSpuNo());
			detailDTO.setQty(dmpSoDetailEntity.getQty());
			detailDTO.setTaxRate(taxRate);
			detailDTO.setTaxPrice(dmpSoDetailEntity.getSellPriceOrigin());
			detailDTO.setCustomerPO(dmpSoInfoEntity.getPlatformCode());
			detailDTO.setToCountry(dmpSoInfoEntity.getSellRemark());
			detailDTO.setPrice(calcPriceExcludeTax(detailDTO.getTaxPrice(), taxRate));
			details.add(detailDTO);
		}
		combineDetailMap.forEach((groupKey,list)->{
			DmpSoDetailEntity dmpSoDetailEntity = list.get(0);
			PlatformB2bOrderDetailDTO detailDTO = new PlatformB2bOrderDetailDTO();
			detailDTO.setSkuNo(dmpSoDetailEntity.getSuiteNo());
			detailDTO.setPlatformSkuNo(dmpSoDetailEntity.getPlatformSpuNo());
			detailDTO.setCustomerPO(dmpSoInfoEntity.getPlatformCode());
			detailDTO.setToCountry(dmpSoInfoEntity.getSellRemark());
			detailDTO.setCustomerSkuNo(dmpSoDetailEntity.getPlatformSpuNo());
			Integer totalQty = dmpSoDetailEntity.getSuiteQty();
			detailDTO.setQty(totalQty);
			detailDTO.setTaxRate(taxRate);
			detailDTO.setTaxPrice(getCombineDetailTaxPrice(dmpSoDetailEntity, list));
			detailDTO.setPrice(calcPriceExcludeTax(detailDTO.getTaxPrice(), taxRate));
			details.add(detailDTO);
		});
		platformB2bOrderDTO.setDetail(details);
		return platformB2bOrderDTO;
	}

	/**
	 * 解析旺店通京东自营 B2B 税率：源税率为空或≤0 时，取 OMS 字典 {@code wdtJdB2bDefaultTaxRate} 的 value。
	 *
	 * @param sourceTaxRate 旺店通原单税率（百分比）
	 * @return 有效税率（百分比）
	 */
	private BigDecimal resolveWdtJdB2bTaxRate(BigDecimal sourceTaxRate) {
		if (sourceTaxRate != null && sourceTaxRate.compareTo(BigDecimal.ZERO) > 0) {
			return sourceTaxRate;
		}
		return getDefaultTaxRateFromDict();
	}

	/**
	 * 从 OMS 字典表读取旺店通京东 B2B 默认税率；查不到或非法时兜底 13。
	 *
	 * @return 默认税率（百分比）
	 */
	private BigDecimal getDefaultTaxRateFromDict() {
		if (cachedDefaultTaxRate != null) {
			return cachedDefaultTaxRate;
		}
		BigDecimal fallback = new BigDecimal("13");
		try {
			List<DictBasicEntity> dictList = FeignQuery.create(DictBasicEntity.class)
					.eq(DictBasicEntity::getType, DictBasicTypeEnum.WDT_JD_B2B_DEFAULT_TAX_RATE.getType())
					.eq(DictBasicEntity::getStatus, true)
					.list();
			if (CollUtil.isNotEmpty(dictList) && StringUtils.isNotBlank(dictList.get(0).getValue())) {
				BigDecimal rate = new BigDecimal(dictList.get(0).getValue().trim());
				if (rate.compareTo(BigDecimal.ZERO) > 0) {
					cachedDefaultTaxRate = rate;
					return cachedDefaultTaxRate;
				}
			}
			log.warn("旺店通京东B2B默认税率字典未配置或非法，type={}，使用兜底{}",
					DictBasicTypeEnum.WDT_JD_B2B_DEFAULT_TAX_RATE.getType(), fallback);
		} catch (Exception e) {
			log.warn("读取旺店通京东B2B默认税率字典失败，使用兜底{}", fallback, e);
		}
		cachedDefaultTaxRate = fallback;
		return cachedDefaultTaxRate;
	}

	/**
	 * 根据含税单价与税率反算不含税单价：price = taxPrice / (1 + taxRate/100)。
	 *
	 * @param taxPrice 含税单价（旺店通原价）
	 * @param taxRate  税率（百分比，如 13）
	 * @return 不含税单价，保留 2 位小数
	 */
	private BigDecimal calcPriceExcludeTax(BigDecimal taxPrice, BigDecimal taxRate) {
		if (taxPrice == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal rate = taxRate == null ? BigDecimal.ZERO : taxRate;
		BigDecimal onePlusTax = BigDecimal.ONE.add(rate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
		return taxPrice.divide(onePlusTax, 2, RoundingMode.HALF_UP);
	}

	private String getCombineDetailGroupKey(DmpSoDetailEntity dmpSoDetailEntity) {
		String suiteNo = dmpSoDetailEntity.getSuiteNo();
		String platformDetailId = dmpSoDetailEntity.getPlatformDetailId();
		if (StringUtils.isNotBlank(suiteNo) && StringUtils.isNotBlank(platformDetailId)) {
			return platformDetailId + "|" + suiteNo;
		}
		return suiteNo;
	}

	private BigDecimal getCombineDetailTaxPrice(DmpSoDetailEntity dmpSoDetailEntity, List<DmpSoDetailEntity> detailList) {
		Integer suiteQty = dmpSoDetailEntity.getSuiteQty();
		if (suiteQty != null && suiteQty != 0) {
			List<BigDecimal> shareAmountList = detailList.stream().map(DmpSoDetailEntity::getAfterAmount).filter(Objects::nonNull).collect(Collectors.toList());
			if (CollUtil.isNotEmpty(shareAmountList)) {
				BigDecimal shareAmount = shareAmountList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
				return shareAmount.divide(new BigDecimal(suiteQty), 4, RoundingMode.HALF_UP);
			}
		}
		return detailList.stream().map(DmpSoDetailEntity::getSellPriceOrigin).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("code");
    }
}
