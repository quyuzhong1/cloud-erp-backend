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
 * 下载平台入库数据消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PLATFORM_OUTBOUND_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.RESTCLOUD_PLATFORM_OUTBOUND_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_PLATFORM_OUTBOUND_TO_WMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class RestCloudPlatformNewOutboundConsumerService extends AbstractRestCloudPlatformConsumerHandler {
	@Resource
	private PlatformOutboundConsumerService platformOutboundConsumerService;
	
	@Override
	public String getBizName() {
		return "平台出库";
	}

	@Override
	public void handle(String data) {
		platformOutboundConsumerService.handle(data);
	}

}