package com.erp.server.wms.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 下载平台库存消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PLATFORM_INVENTORY_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.RESTCLOUD_PLATFORM_INVENTORY_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_PLATFORM_INVENTORY_TO_WMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewInventoryConsumerService extends AbstractNewPlatformConsumerHandler {
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