package com.erp.server.wms.rocketmq.consumer;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
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
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_OUTBOUND_TO_OMS_PUSH_DOWN_WMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_PLATFORM_OUTBOUND_TO_OMS_PUSH_DOWN_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_OUTBOUND_TO_OMS_PUSH_DOWN_WMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewOutboundToOmsPushDownWmsConsumerService extends AbstractNewPlatformConsumerHandler {
	@Resource
	private PlatformOutboundToOmsPushDownWmsConsumerService platformOutboundToOmsPushDownWmsConsumerService;
	
	@Override
	public String getBizName() {
		return "三方仓自动出库";
	}

	@Override
	public void handle(String data) {
		platformOutboundToOmsPushDownWmsConsumerService.handle(data);
	}

}