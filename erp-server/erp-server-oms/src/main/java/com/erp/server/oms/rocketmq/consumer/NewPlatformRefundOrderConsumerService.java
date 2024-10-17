package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformRefundOrderDTO;
import com.common.business.dto.PlatformReturnOrderDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.RefundOrderStatusEnum;
import com.erp.server.oms.service.RefundOrderService;
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
	private RefundOrderService refundOrderService;

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
		PlatformRefundOrderDTO dto = JSONUtil.toBean(data, PlatformRefundOrderDTO.class);
		if(Objects.isNull(dto)){
			return;
		}
		RefundOrderEntity exist = refundOrderService.getByPlatformRefundCode(dto.getPlatformRefundNo());
		if(Objects.nonNull(exist)){
			return;
		}
		SoB2cEntity soB2cEntity = null;
		List<SoB2cDetailEntity> soB2cDetailEntityList = new ArrayList<>();
		if(StringUtils.isNotBlank(dto.getPlatformOrderNo())){
			List<SoB2cEntity> soB2cEntityList = soB2cService.getByPlatformCode(dto.getPlatformOrderNo());
			if(CollectionUtils.isNotEmpty(soB2cEntityList)){
				soB2cEntity = soB2cEntityList.get(0);
				soB2cDetailEntityList = soB2cDetailService.listByMainId(soB2cEntity.getId());
			}
		}
		RefundOrderEntity refundOrderEntity = this.buildRefund(dto,soB2cEntity);
		List<RefundOrderDetailEntity> refundOrderDetailEntityList = this.buildRefundDetail(dto,soB2cDetailEntityList);
		refundOrderService.add(refundOrderEntity,refundOrderDetailEntityList);
	}

	private List<RefundOrderDetailEntity> buildRefundDetail(PlatformRefundOrderDTO dto, List<SoB2cDetailEntity> soB2cDetailEntityList) {
		List<RefundOrderDetailEntity> list = new ArrayList<>();
		for (PlatformRefundOrderDTO.Detail detail : dto.getDetailList()) {
			RefundOrderDetailEntity refundOrderDetailEntity = new RefundOrderDetailEntity();
			refundOrderDetailEntity.setPlatformSkuNo(detail.getPlatformSkuNo());
			refundOrderDetailEntity.setRefundQty(detail.getRefundQty());
			SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(item -> item.getPlatformSkuNo().equals(detail.getPlatformSkuNo())).findFirst().orElse(null);
			if(Objects.nonNull(soB2cDetailEntity)){
				refundOrderDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
				refundOrderDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
				refundOrderDetailEntity.setSaleQty(soB2cDetailEntity.getQty());
			}
			list.add(refundOrderDetailEntity);
		}

		return list;
	}

	private RefundOrderEntity buildRefund(PlatformRefundOrderDTO dto,SoB2cEntity soB2cEntity) {
		RefundOrderEntity refundOrderEntity = new RefundOrderEntity();
		refundOrderEntity.setDictPlatform(dto.getDictPlatform());
		refundOrderEntity.setPlatformOrderNo(dto.getPlatformOrderNo());
		refundOrderEntity.setPlatformRefundNo(dto.getPlatformRefundNo());
		refundOrderEntity.setStatus(RefundOrderStatusEnum.FINISH.getCode());
		refundOrderEntity.setRefundAmount(dto.getRefundAmount());
		refundOrderEntity.setCurrency(dto.getCurrency());
		refundOrderEntity.setReason(dto.getRemark());
		refundOrderEntity.setRefundTime(dto.getRefundTime());
		refundOrderEntity.setPlatformCreateTime(dto.getCreateTime());
		if(Objects.nonNull(soB2cEntity)){
			refundOrderEntity.setShopId(soB2cEntity.getShopId());
			ShopInfoEntity shopInfo = shopInfoService.getById(soB2cEntity.getShopId());
			refundOrderEntity.setShopName(shopInfo.getName());
			refundOrderEntity.setSoId(soB2cEntity.getId());
			refundOrderEntity.setSoCode(soB2cEntity.getCode());
		}
        return refundOrderEntity;
	}

}