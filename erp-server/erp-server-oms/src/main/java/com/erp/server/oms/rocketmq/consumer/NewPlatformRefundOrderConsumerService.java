package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformRefundOrderDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.RefundOrderStatusEnum;
import com.erp.server.oms.service.SoB2cRefundService;
import com.erp.server.oms.service.SkuMappingService;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.SoB2cDetailService;
import com.erp.server.oms.service.SoB2cService;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 平台退款订单单消费者
 * @author Cloud
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_REFUND_ORDER_TO_OMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_PLATFORM_REFUND_ORDER_TO_OMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_REFUND_ORDER_TO_OMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class NewPlatformRefundOrderConsumerService extends AbstractNewPlatformConsumerHandler{

	@Resource
	private SoB2cRefundService soB2cRefundService;

	@Resource
	private SoB2cService soB2cService;

	@Resource
	private SoB2cDetailService soB2cDetailService;

	@Resource
	private ShopInfoService shopInfoService;

	@Resource
	private SkuMappingService skuMappingService;

	@Override
	public String getBizName() {
		return "平台退款订单";
	}
	
    @Override
	public void handle(String data) {
		log.debug("平台退货入库单消费:{}", data);
		PlatformRefundOrderDTO dto = JSONUtil.toBean(data, PlatformRefundOrderDTO.class);
		if(Objects.isNull(dto)){
			return;
		}
		SoB2cRefundEntity exist = soB2cRefundService.getByPlatformRefundCode(dto.getPlatformRefundNo());
		if(Objects.nonNull(exist)){
			return;
		}
		SoB2cEntity soB2cEntity = null;
		List<SoB2cDetailEntity> soB2cDetailEntityList = new ArrayList<>();
		if(StringUtils.isNotBlank(dto.getPlatformOrderNo())){
			List<SoB2cEntity> soB2cEntityList = soB2cService.getByPlatformCode(dto.getPlatformOrderNo());
			if (StringUtils.isNotBlank(dto.getShopId())) {
				soB2cEntityList = soB2cEntityList.stream()
						.filter(v -> Objects.equals(v.getShopId(), dto.getShopId()))
						.collect(Collectors.toList());
			}
			//过滤手工单
			soB2cEntityList = soB2cEntityList.stream().filter(v-> !SourceTypeEnum.SELF_ADD.getCode().equals(v.getSourceType())).collect(Collectors.toList());

			if(CollectionUtils.isNotEmpty(soB2cEntityList)){
				soB2cEntity = soB2cEntityList.get(0);
				soB2cDetailEntityList = soB2cDetailService.listByMainId(soB2cEntity.getId());
			}
		}
		SoB2cRefundEntity soB2cRefundEntity = this.buildRefund(dto,soB2cEntity);
		List<SoB2cRefundDetailEntity> soB2cRefundDetailEntityList = this.buildRefundDetail(dto,soB2cDetailEntityList, soB2cEntity);
		soB2cRefundService.autoAddApprove(soB2cRefundEntity, soB2cRefundDetailEntityList);
	}

	private List<SoB2cRefundDetailEntity> buildRefundDetail(PlatformRefundOrderDTO dto,
			List<SoB2cDetailEntity> soB2cDetailEntityList,
			SoB2cEntity soB2cEntity) {
		List<SoB2cRefundDetailEntity> list = new ArrayList<>();
		String shopId = Objects.nonNull(soB2cEntity) ? soB2cEntity.getShopId() : dto.getShopId();
		for (PlatformRefundOrderDTO.Detail detail : dto.getDetailList()) {
			SoB2cRefundDetailEntity soB2cRefundDetailEntity = new SoB2cRefundDetailEntity();
			soB2cRefundDetailEntity.setPlatformSkuNo(detail.getPlatformSkuNo());
			soB2cRefundDetailEntity.setRefundQty(detail.getRefundQty());
			SoB2cDetailEntity soB2cDetailEntity = resolveSoDetail(detail.getPlatformSkuNo(), soB2cDetailEntityList, dto.getDictPlatform());
			if(Objects.nonNull(soB2cDetailEntity)){
				soB2cRefundDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
				soB2cRefundDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
				soB2cRefundDetailEntity.setSaleQty(soB2cDetailEntity.getQty());
			} else {
				ListingInfoWithSkuMappingDTO mappingDTO = resolveMappingByPlatformSku(detail.getPlatformSkuNo(), dto.getDictPlatform(), shopId);
				if (Objects.nonNull(mappingDTO)) {
					soB2cRefundDetailEntity.setSkuId(mappingDTO.getProductSkuId());
					soB2cRefundDetailEntity.setSkuNo(mappingDTO.getProductSkuNo());
				}
			}
			list.add(soB2cRefundDetailEntity);
		}

		return list;
	}

	private SoB2cDetailEntity resolveSoDetail(String platformSkuNo, List<SoB2cDetailEntity> soB2cDetailEntityList, String dictPlatform) {
		if (CollectionUtils.isEmpty(soB2cDetailEntityList) || StringUtils.isBlank(platformSkuNo)) {
			return null;
		}
		SoB2cDetailEntity matchedDetail = soB2cDetailEntityList.stream()
				.filter(item -> Objects.equals(item.getPlatformSkuNo(), platformSkuNo))
				.findFirst()
				.orElse(null);
		if (Objects.nonNull(matchedDetail)) {
			return matchedDetail;
		}
		if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dictPlatform)) {
			return soB2cDetailEntityList.stream()
					.filter(item -> Objects.equals(item.getPlatformSpuNo(), platformSkuNo))
					.findFirst()
					.orElse(null);
		}
		return null;
	}

	private ListingInfoWithSkuMappingDTO resolveMappingByPlatformSku(String platformSkuNo, String dictPlatform, String shopId) {
		if (StringUtils.isBlank(platformSkuNo) || StringUtils.isBlank(dictPlatform) || StringUtils.isBlank(shopId)) {
			return null;
		}
		if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dictPlatform)) {
			List<ListingInfoWithSkuMappingDTO> mappingDTOList = skuMappingService
					.mapListingByPlatformSkuNo(java.util.Collections.singletonList(""),
							java.util.Collections.singletonList(platformSkuNo),
							dictPlatform,
							shopId,
							null,
							null)
					.values()
					.stream()
					.flatMap(List::stream)
					.collect(Collectors.toList());
			if (CollectionUtils.isEmpty(mappingDTOList)) {
				return null;
			}
			return skuMappingService.checkAndMappingDTO(mappingDTOList, platformSkuNo, dictPlatform, "");
		}
		List<ListingInfoWithSkuMappingDTO> mappingDTOList = skuMappingService
				.mapListingByPlatformSkuNo(java.util.Collections.singletonList(platformSkuNo),
						java.util.Collections.emptyList(),
						dictPlatform,
						shopId,
						null,
						null)
				.get(platformSkuNo);
		if (CollectionUtils.isEmpty(mappingDTOList)) {
			return null;
		}
		return skuMappingService.checkAndMappingDTO(mappingDTOList, null, dictPlatform, platformSkuNo);
	}

	private SoB2cRefundEntity buildRefund(PlatformRefundOrderDTO dto, SoB2cEntity soB2cEntity) {
		SoB2cRefundEntity soB2cRefundEntity = new SoB2cRefundEntity();
		soB2cRefundEntity.setDictPlatform(dto.getDictPlatform());
		soB2cRefundEntity.setPlatformOrderNo(dto.getPlatformOrderNo());
		soB2cRefundEntity.setPlatformRefundNo(dto.getPlatformRefundNo());
		soB2cRefundEntity.setStatus(RefundOrderStatusEnum.FINISH.getCode());
		soB2cRefundEntity.setRefundAmount(dto.getRefundAmount());
		soB2cRefundEntity.setCurrency(dto.getCurrency());
		soB2cRefundEntity.setReason(dto.getRemark());
		soB2cRefundEntity.setRefundTime(dto.getRefundTime());
		soB2cRefundEntity.setPlatformCreateTime(dto.getPlatformCreateTime());
		if(Objects.nonNull(soB2cEntity)){
			soB2cRefundEntity.setShopId(soB2cEntity.getShopId());
			ShopInfoEntity shopInfo = shopInfoService.getById(soB2cEntity.getShopId());
			soB2cRefundEntity.setShopName(shopInfo.getName());
			soB2cRefundEntity.setSoId(soB2cEntity.getId());
			soB2cRefundEntity.setSoCode(soB2cEntity.getCode());
		}
        return soB2cRefundEntity;
	}

}
