package com.erp.server.oms.rocketmq.consumer;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * restCloud下载平台商品消费服务
 * @author will
 * @date 2025/9/22 12:22
 */
@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_PRODUCT_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_PLATFORM_REST_CLOUD_PRODUCT_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_PRODUCT_TO_OMS_GROUP)
public class NewPlatformRestCloudListingConsumerService extends AbstractRestCloudPlatformConsumerHandler {
	@Resource
	private PlatformListingConsumerService platformListingConsumerService;

	@Override
	public String getBizName() {
		return "restCloud销售平台产品";
	}
	
    @Override
	public void handle(String data) {
		platformListingConsumerService.handle(data);
	}

}
