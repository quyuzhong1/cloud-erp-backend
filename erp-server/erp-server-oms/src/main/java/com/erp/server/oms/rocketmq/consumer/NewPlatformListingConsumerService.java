package com.erp.server.oms.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;

/**
 * 下载平台商品消费服务
 *
 * @author Jim
 */
@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PLATFORM_PRODUCT_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_PLATFORM_PRODUCT_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_PLATFORM_PRODUCT_TO_OMS_GROUP)
public class NewPlatformListingConsumerService extends AbstractNewPlatformConsumerHandler{
	@Resource
	private PlatformListingConsumerService platformListingConsumerService;

	@Override
	public String getBizName() {
		return "销售平台产品";
	}
	
    @Override
	public void handle(String data) {
		platformListingConsumerService.handle(data);
	}

}
