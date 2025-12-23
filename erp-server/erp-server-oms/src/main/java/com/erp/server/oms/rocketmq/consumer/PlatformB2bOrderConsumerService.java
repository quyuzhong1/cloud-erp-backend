package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformB2bOrderDTO;
import com.common.business.dto.PlatformB2bOrderDetailDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.CustomerAddressTypeEnum;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.oms.enums.ShopOrderRouteEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * b2b销售单
 *
 */
@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_B2B_ORDER_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_PLATFORM_B2B_ORDER_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_B2B_ORDER_TO_OMS_GROUP)
@Slf4j
public class PlatformB2bOrderConsumerService extends AbstractNewPlatformConsumerHandler {

	@Resource
	private CustomerInfoService customerInfoService;

	@Resource
	private SkuMappingService skuMappingService;

	@Resource
	private CustomerAddressService customerAddressService;

	@Resource
	private SysUserFeign sysUserFeign;

	@Resource
	private SoInfoService soInfoService;

	@Resource
	private ShopInfoService shopInfoService;

	@Resource
	private PlmTaskFeign plmTaskFeign;

	@Override
	public String getBizName() {
		return "b2b销售订单";
	}

    @Override
	@Transactional(rollbackFor = Exception.class)
	public void handle(String data) {
		PlatformB2bOrderDTO dto = JSONUtil.toBean(data.toString(), PlatformB2bOrderDTO.class);
		if(dto == null) {
			log.error("  b2b订单消费失败，参数为空");
			return;
		}
		this.fillDTO(dto);
		if(PlatformDictEnum.DHT.getCode().equals(dto.getThirdSystem()) &&(StringUtils.isBlank(dto.getCustomerId()))){
			//不用清洗
			log.error("  b2b订单消费失败，未匹配到客户，客户编码：{}",dto.getCustomerCode());
			return;
		}
		soInfoService.handlePlatformConsumer(dto);
	}


	private void fillDTO(PlatformB2bOrderDTO dto) {
		if(PlatformDictEnum.DHT.getCode().equals(dto.getThirdSystem())){
			fillDhtDTO(dto);
		}else if(PlatformDictEnum.WDT.getCode().equals(dto.getThirdSystem())){
			fillWdtDTO(dto);
		}
	}

	private void fillWdtDTO(PlatformB2bOrderDTO dto) {
		dto.setIsDeclare(false);
		//查询店铺
		String shopId = dto.getShopId();
		ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
		if(!ShopOrderRouteEnum.B2B.getCode().equals(shopInfo.getOrderRouteType())){
			throw new ServiceException("非b2b订单，不处理");
		}
		String customerId = shopInfo.getCustomerId();
		CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
		if(Objects.isNull(customerInfo)){
			throw new ServiceException("未匹配到客户，客户id："+customerId);
		}
		dto.setCurrency(customerInfo.getCurrency());
		dto.setCustomerId(customerInfo.getId());
		dto.setSalesOrgId(customerInfo.getUseOrgId());
		dto.setSalesDeptId(customerInfo.getSalesDeptId());
		dto.setSellerId(customerInfo.getSellerId());
		dto.setCountryId(customerInfo.getCountryId());
		dto.setReceiveCondition(customerInfo.getConditionDict());
		CustomerAddressEntity customerAddressEntity = customerAddressService.getDefaultAddrByMainId(customerInfo.getId());
		if(Objects.nonNull(customerAddressEntity)){
			dto.setCustomerAddressId(customerAddressEntity.getId());
			dto.setReceiverName(customerAddressEntity.getPerson());
			dto.setTelNumber(customerAddressEntity.getTelNumber());
			dto.setCountryId(customerAddressEntity.getCountryId());
			dto.setCountryName(customerAddressEntity.getCountryName());
		}

		dto.setDictPlatform(customerInfo.getPlatformType());
		dto.setAddressType(CustomerAddressTypeEnum.FORWARDER.getCode());
		dto.setDeliveryMode(DeliveryModeEnum.EXPRESS.getCode());
		List<PlatformB2bOrderDetailDTO> platformB2bOrderDetailDTOS = dto.getDetail();
		List<String> skuNoList = platformB2bOrderDetailDTOS.stream().map(PlatformB2bOrderDetailDTO::getSkuNo).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
		List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
		for (PlatformB2bOrderDetailDTO platformB2bOrderDetailDTO : platformB2bOrderDetailDTOS) {
			SkuVO skuVO = skuVOList.stream().filter(e -> e.getSkuNo().equals(platformB2bOrderDetailDTO.getSkuNo())).findFirst().orElse(null);
			if(Objects.nonNull(skuVO)){
				platformB2bOrderDetailDTO.setSkuId(skuVO.getSkuId());
			}else{
				throw new ServiceException("未匹配到产品，产品编码："+platformB2bOrderDetailDTO.getSkuNo());
			}

		}
	}

	private void fillDhtDTO(PlatformB2bOrderDTO dto) {
		dto.setDictPlatform(dto.getThirdSystem());
		dto.setAddressType(CustomerAddressTypeEnum.DELIVER.getCode());
		//匹配中台erp的仓库
		if(StringUtils.isNotBlank(dto.getPlatformWarehouseName())){
			List<WarehouseEntity> warehouseEntity = FeignQuery.create(WarehouseEntity.class).eq(WarehouseEntity::getName, dto.getPlatformWarehouseName()).list();
			if(CollectionUtils.isNotEmpty(warehouseEntity)){
				dto.setWarehouseId(warehouseEntity.get(0).getId());
			}
		}
		SysAccountingCompanyEntity sysAccountingCompanyEntity = null;
		if(StringUtils.isNotBlank(dto.getKindgeeOrgId())){
			sysAccountingCompanyEntity = sysUserFeign.getCompanyByKindgeeId(dto.getKindgeeOrgId());
			if(Objects.nonNull(sysAccountingCompanyEntity)){
				dto.setSalesOrgId(sysAccountingCompanyEntity.getId());
			}
		}
		//通过客户编号匹配客户
		if(StringUtils.isNotBlank(dto.getCustomerCode())){
			CustomerInfoEntity customerInfo = customerInfoService.getCustomerByCode(dto.getCustomerCode());
			if(Objects.isNull(customerInfo)){
				return;
			}
			dto.setCustomerId(customerInfo.getId());
			if(StringUtils.isBlank(dto.getSalesOrgId())){
				dto.setSalesOrgId(customerInfo.getUseOrgId());
			}
			dto.setSalesDeptId(customerInfo.getSalesDeptId());
			dto.setSellerId(customerInfo.getSellerId());
			dto.setCountryId(customerInfo.getCountryId());
			dto.setReceiveCondition(customerInfo.getConditionDict());
			// 通过收货地址匹配客户地址表
			if(StringUtils.isNotBlank(dto.getReceiveAddress())){
				CustomerAddressEntity customerAddressEntity = customerAddressService.lambdaQuery().eq(CustomerAddressEntity::getMainId,customerInfo.getId()).eq(CustomerAddressEntity::getAddress, dto.getReceiveAddress()).last("LIMIT 1").one();
				dto.setCustomerAddressId(customerAddressEntity.getId());
				dto.setReceiverName(customerAddressEntity.getPerson());
				dto.setTelNumber(customerAddressEntity.getTelNumber());
			}
			dto.setIsDeclare(false);
		}
		//过滤掉明细已删除和已作废
		if(CollectionUtils.isNotEmpty(dto.getDetail())) {
			List<PlatformB2bOrderDetailDTO> details = dto.getDetail().stream().filter(e -> !e.getPlatformIsDeleted() && !e.getIsInvalid()).collect(Collectors.toList());
			dto.setDetail(details);
		}
		//处理明细
		List<PlatformB2bOrderDetailDTO>  platformB2bOrderDetailDTOS = dto.getDetail();
		List<String> platformSkuNoList = platformB2bOrderDetailDTOS.stream().map(PlatformB2bOrderDetailDTO::getPlatformSkuNo).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setPlatform(dto.getThirdSystem());
		List<ListingInfoWithSkuMappingDTO> mappingDTOList = skuMappingService.findListDto(listingInfoParamDTO);
		if(Objects.isNull(sysAccountingCompanyEntity) && StringUtils.isNotBlank(dto.getSalesOrgId())){
			sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getSalesOrgId());
			if(Objects.nonNull(sysAccountingCompanyEntity) && sysAccountingCompanyEntity.getVatRate().compareTo(BigDecimal.ZERO)>0){
				dto.setIsTax(true);
			}else{
				dto.setIsTax(false);
			}
		}
		for (PlatformB2bOrderDetailDTO platformB2bOrderDetailDTO : platformB2bOrderDetailDTOS) {
			if(StringUtils.isNotBlank(platformB2bOrderDetailDTO.getPlatformSkuNo())) {
				List<ListingInfoWithSkuMappingDTO> collect = mappingDTOList.stream().filter(e -> e.getPlatformSkuNo().equals(platformB2bOrderDetailDTO.getPlatformSkuNo())).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(collect)) {
					platformB2bOrderDetailDTO.setSkuId(collect.get(0).getProductSkuId());
					platformB2bOrderDetailDTO.setSkuNo(collect.get(0).getProductSkuNo());
				}
			}
			if(Objects.nonNull(sysAccountingCompanyEntity) && Objects.nonNull(sysAccountingCompanyEntity.getVatRate())){
				platformB2bOrderDetailDTO.setTaxRate(sysAccountingCompanyEntity.getVatRate());
			}else{
				platformB2bOrderDetailDTO.setTaxRate(BigDecimal.ZERO);
			}
			//计算含税单价
			BigDecimal taxRate = platformB2bOrderDetailDTO.getTaxRate().divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);
			BigDecimal taxPrice = platformB2bOrderDetailDTO.getTaxPrice();
			BigDecimal onePlusTax = BigDecimal.ONE.add(taxRate);
			BigDecimal price = taxPrice.divide(onePlusTax, 2, RoundingMode.HALF_UP);
			platformB2bOrderDetailDTO.setPrice(price);
		}
		//通过客户id 和产品sku查到客户sku
		List<String> skuIdList = platformB2bOrderDetailDTOS.stream().map(PlatformB2bOrderDetailDTO::getSkuId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
		if(CollectionUtils.isNotEmpty(skuIdList)){
			ListingInfoParamDTO customerListingDTO = new ListingInfoParamDTO();
			customerListingDTO.setSkuIdList(skuIdList);
			customerListingDTO.setAuthId(dto.getCustomerId());
			customerListingDTO.setType(RuleTypeEnum.CUSTOMER.getCode());
			List<ListingInfoWithSkuMappingDTO> customerMappingDTOList = skuMappingService.findListDto(customerListingDTO);
			if(CollectionUtils.isNotEmpty(customerMappingDTOList)){
				Map<String, List<ListingInfoWithSkuMappingDTO>> customerSkuMap = customerMappingDTOList.stream().collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getProductSkuId));
				for (PlatformB2bOrderDetailDTO platformB2bOrderDetailDTO : platformB2bOrderDetailDTOS) {
					if(StringUtils.isNotBlank(platformB2bOrderDetailDTO.getSkuId()) && customerSkuMap.containsKey(platformB2bOrderDetailDTO.getSkuId())){
						List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOS = customerSkuMap.get(platformB2bOrderDetailDTO.getSkuId());
						if(CollectionUtils.isNotEmpty(listingInfoWithSkuMappingDTOS)){
							platformB2bOrderDetailDTO.setCustomerSkuNo(listingInfoWithSkuMappingDTOS.get(0).getPlatformSkuNo());
						}
					}
				}
			}
		}
	}


}
