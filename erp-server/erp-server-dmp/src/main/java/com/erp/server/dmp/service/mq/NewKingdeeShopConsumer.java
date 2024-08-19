package com.erp.server.dmp.service.mq;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.server.dmp.service.BiDmpShopInfoService;

@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_KINGDEE_SHOP_TO_DMP_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_KINGDEE_SHOP_TO_DMP_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_KINGDEE_SHOP_TO_DMP_GROUP)
public class NewKingdeeShopConsumer extends AbstractNewPlatformConsumerHandler {

	@Resource
	private BiDmpShopInfoService biDmpShopInfoService;

	@Override
	public String getBizName() {
		return "金蝶店铺";
	}
	
	@Override
	public void handle(String data) {
		biDmpShopInfoService.checkShopByKingDee(JSON.parseObject(data, BiShopInfoEntity.class));
	}
}