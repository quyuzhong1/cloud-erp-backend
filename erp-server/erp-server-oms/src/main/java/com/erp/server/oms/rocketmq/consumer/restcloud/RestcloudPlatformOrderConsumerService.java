package com.erp.server.oms.rocketmq.consumer.restcloud;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.server.oms.rocketmq.consumer.PlatformOrderConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 下载平台订单消费服务
 * @author Cloud
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PLATFORM_ORDER_TO_OMS_TOPIC,
selectorExpression = RocketMqNewTag.RESTCLOUD_PLATFORM_ORDER_TO_OMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_PLATFORM_ORDER_TO_OMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class RestcloudPlatformOrderConsumerService extends AbstractRestCloudPlatformConsumerHandler {
	@Resource
	private PlatformOrderConsumerService platformOrderConsumerService;

	@Override
	public String getBizName() {
		return "销售平台订单";
	}
	
    @Override
	public void handle(String data) {
    	platformOrderConsumerService.handle(data);
	}

}