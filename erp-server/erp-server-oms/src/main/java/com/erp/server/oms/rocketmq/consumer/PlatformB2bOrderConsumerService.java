package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformB2bOrderDTO;
import com.common.business.dto.PlatformB2bOrderDetailDTO;
import com.common.business.dto.PlatformReceiptDTO;
import com.common.business.dto.PlatformReceiptDetailDTO;
import com.common.core.enums.CountrySiteEnum;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
public class PlatformB2bOrderConsumerService extends AbstractRestCloudPlatformConsumerHandler {

	@Resource
	private CustomerInfoService customerInfoService;

	@Resource
	private SkuMappingService skuMappingService;

	@Resource
	private CustomerAddressService customerAddressService;

	@Resource
	private SoInfoService soInfoService;

	@Resource
	private DmpThirdMappingFeign dmpThirdMappingFeign;

	@Resource
	private DictBasicService dictBasicService;

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
		if(Objects.isNull(dto.getErpInfoDTO()) || StringUtils.isBlank(dto.getErpInfoDTO().getCustomerId())){
			//不用清洗
			log.error("  b2b订单消费失败，未匹配到客户，客户编码：{}",dto.getCustomerCode());
			return;
		}
		soInfoService.handlePlatformConsumer(dto);
	}


	private void fillDTO(PlatformB2bOrderDTO dto) {
		PlatformB2bOrderDTO.ErpInfoDTO erpInfoDTO = new PlatformB2bOrderDTO.ErpInfoDTO();

		//匹配中台erp的仓库
		if(StringUtils.isNotBlank(dto.getPlatformWarehouseId())){
			ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
			viewParamDTO.setType(ThirdSysTypeEnum.WAREHOUSE.getCode());
			viewParamDTO.setThirdId(dto.getPlatformWarehouseId());
			viewParamDTO.setSysType(dto.getThirdSystem());
			List<ThirdMappingEntity> thirdMappingEntities = dmpThirdMappingFeign.getByThirdId(viewParamDTO);
			if(CollectionUtils.isNotEmpty(thirdMappingEntities)){
				erpInfoDTO.setWarehouseId(thirdMappingEntities.get(0).getSysId());
			}else{
				//默认仓库
				List<DictBasicDTO.ViewDTO> viewDTOList = dictBasicService.getByKey("dhtDefaultWarehouse");
				if(CollectionUtils.isNotEmpty(viewDTOList)){
					erpInfoDTO.setWarehouseId(viewDTOList.get(0).getValue());
				}
			}
		}
		//通过客户编号匹配客户
		if(StringUtils.isNotBlank(dto.getCustomerCode())){
			CustomerInfoEntity customerInfo = customerInfoService.getCustomerByCode(dto.getCustomerCode());
			if(Objects.isNull(customerInfo)){
				return;
			}
			erpInfoDTO.setCustomerId(customerInfo.getId());
			erpInfoDTO.setSalesOrgId(customerInfo.getUseOrgId());
			erpInfoDTO.setSalesDeptId(customerInfo.getSalesDeptId());
			erpInfoDTO.setSellerId(customerInfo.getSellerId());
			erpInfoDTO.setCountryId(customerInfo.getCountryId());
			erpInfoDTO.setReceiveCondition(customerInfo.getConditionDict());
			// 通过收货地址匹配客户地址表
			if(StringUtils.isNotBlank(dto.getReceiveAddress())){
				CustomerAddressEntity customerAddressEntity = customerAddressService.lambdaQuery().eq(CustomerAddressEntity::getMainId,customerInfo.getId()).eq(CustomerAddressEntity::getAddress,dto.getReceiveAddress()).last("LIMIT 1").one();
				erpInfoDTO.setCustomerAddressId(customerAddressEntity.getId());
				erpInfoDTO.setReceiverName(customerAddressEntity.getPerson());
				erpInfoDTO.setTelNumber(customerAddressEntity.getTelNumber());
			}
			erpInfoDTO.setIsDeclare(customerInfo.getCountryId().equals(CountrySiteEnum.CHINA.getSite()));
		}
		//过滤掉明细已删除和已作废
		if(CollectionUtils.isNotEmpty(dto.getDetail())) {
			List<PlatformB2bOrderDetailDTO> details = dto.getDetail().stream().filter(e -> !e.getPlatformIsDeleted() && !e.getIsInvalid()).collect(Collectors.toList());
			dto.setDetail(details);
		}
		dto.setErpInfoDTO(erpInfoDTO);
		//处理明细
		List<PlatformB2bOrderDetailDTO>  platformB2bOrderDetailDTOS = dto.getDetail();
		List<String> platformSkuNoList = platformB2bOrderDetailDTOS.stream().map(PlatformB2bOrderDetailDTO::getPlatformSkuNo).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
		ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
		listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
		listingInfoParamDTO.setPlatform(dto.getThirdSystem());
		List<ListingInfoWithSkuMappingDTO> mappingDTOList = skuMappingService.findListDto(listingInfoParamDTO);
		for (PlatformB2bOrderDetailDTO platformB2bOrderDetailDTO : platformB2bOrderDetailDTOS) {
			if(StringUtils.isNotBlank(platformB2bOrderDetailDTO.getPlatformSkuNo())) {
				List<ListingInfoWithSkuMappingDTO> collect = mappingDTOList.stream().filter(e -> e.getPlatformSkuNo().equals(platformB2bOrderDetailDTO.getPlatformSkuNo())).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(collect)) {
					platformB2bOrderDetailDTO.setSkuId(collect.get(0).getProductSkuId());
					platformB2bOrderDetailDTO.setSkuNo(collect.get(0).getProductSkuNo());
				}
			}
		}
		//通过客户id 和产品sku查到客户sku
		List<String> skuIdList = platformB2bOrderDetailDTOS.stream().map(PlatformB2bOrderDetailDTO::getSkuId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
		if(CollectionUtils.isNotEmpty(skuIdList)){
			ListingInfoParamDTO customerListingDTO = new ListingInfoParamDTO();
			customerListingDTO.setSkuIdList(skuIdList);
			customerListingDTO.setAuthId(dto.getErpInfoDTO().getCustomerId());
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
