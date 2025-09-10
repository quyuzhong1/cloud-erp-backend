package com.erp.server.oms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.dto.PlatformReceiptDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 收款单
 *
 */
@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_RECEIPT_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_PLATFORM_RECEIPT_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_RECEIPT_TO_OMS_GROUP)
@Slf4j
public class PlatformReceiptConsumerService extends AbstractNewPlatformConsumerHandler{
	@Resource
	private PlatformListingConsumerService platformListingConsumerService;

	@Override
	public String getBizName() {
		return "销售平台产品";
	}
	
    @Override
	@Transactional(rollbackFor = Exception.class)
	public void handle(String data) {
		PlatformReceiptDTO dto = JSONUtil.toBean(data.toString(), PlatformReceiptDTO.class);
		if(dto == null) {
			log.error("PlatformReceiptConsumerService.handle 收款单消费失败，参数为空");
			return;
		}

	}

}
