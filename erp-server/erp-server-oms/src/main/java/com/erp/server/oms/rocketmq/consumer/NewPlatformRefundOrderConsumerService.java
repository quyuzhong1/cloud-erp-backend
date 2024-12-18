package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformRefundOrderDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.RefundOrderStatusEnum;
import com.erp.server.oms.service.SoB2cRefundService;
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
			//过滤手工单
			soB2cEntityList = soB2cEntityList.stream().filter(v-> !SourceTypeEnum.SELF_ADD.getCode().equals(v.getSourceType())).collect(Collectors.toList());

			if(CollectionUtils.isNotEmpty(soB2cEntityList)){
				soB2cEntity = soB2cEntityList.get(0);
				soB2cDetailEntityList = soB2cDetailService.listByMainId(soB2cEntity.getId());
			}
		}
		SoB2cRefundEntity soB2cRefundEntity = this.buildRefund(dto,soB2cEntity);
		List<SoB2cRefundDetailEntity> soB2cRefundDetailEntityList = this.buildRefundDetail(dto,soB2cDetailEntityList);
		soB2cRefundService.add(soB2cRefundEntity, soB2cRefundDetailEntityList);
	}

	private List<SoB2cRefundDetailEntity> buildRefundDetail(PlatformRefundOrderDTO dto, List<SoB2cDetailEntity> soB2cDetailEntityList) {
		List<SoB2cRefundDetailEntity> list = new ArrayList<>();
		for (PlatformRefundOrderDTO.Detail detail : dto.getDetailList()) {
			SoB2cRefundDetailEntity soB2cRefundDetailEntity = new SoB2cRefundDetailEntity();
			soB2cRefundDetailEntity.setPlatformSkuNo(detail.getPlatformSkuNo());
			soB2cRefundDetailEntity.setRefundQty(detail.getRefundQty());
			SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(item -> item.getPlatformSkuNo().equals(detail.getPlatformSkuNo())).findFirst().orElse(null);
			if(Objects.nonNull(soB2cDetailEntity)){
				soB2cRefundDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
				soB2cRefundDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
				soB2cRefundDetailEntity.setSaleQty(soB2cDetailEntity.getQty());
			}
			list.add(soB2cRefundDetailEntity);
		}

		return list;
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