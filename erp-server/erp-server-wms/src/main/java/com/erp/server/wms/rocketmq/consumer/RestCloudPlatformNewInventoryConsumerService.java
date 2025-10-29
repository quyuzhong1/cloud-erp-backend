package com.erp.server.wms.rocketmq.consumer;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 下载平台库存消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_INVENTORY_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_PLATFORM_INVENTORY_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_INVENTORY_TO_WMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class RestCloudPlatformNewInventoryConsumerService extends AbstractRestCloudPlatformConsumerHandler {
	@Resource
	private PlatformInventoryConsumerService platformInventoryConsumerService;
	
	@Override
	public String getBizName() {
		return "平台库存";
	}

	@Override
	public void handle(String data) {
		platformInventoryConsumerService.handle(data);
	}

}