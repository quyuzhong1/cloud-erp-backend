package com.erp.server.oms.rocketmq.consumer.restcloud;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.server.oms.rocketmq.consumer.PlatformListingConsumerService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 下载平台商品消费服务
 *
 * @author Jim
 */
@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PLATFORM_PRODUCT_TO_OMS_TOPIC,
        selectorExpression = RocketMqNewTag.RESTCLOUD_PLATFORM_PRODUCT_TO_OMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.RESTCLOUD_PLATFORM_PRODUCT_TO_OMS_GROUP)
public class RestcloudPlatformListingConsumerService extends AbstractNewPlatformConsumerHandler{
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
