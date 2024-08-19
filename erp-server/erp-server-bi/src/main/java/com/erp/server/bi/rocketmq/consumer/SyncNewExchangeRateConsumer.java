package com.erp.server.bi.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.dto.DmpExchangeRateDTO;
import com.erp.server.bi.rocketmq.sync.SyncExchangeRateService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_KINGDEE_EXCHANGERATE_TO_BI_TOPIC, selectorExpression = RocketMqNewTag.DMP_KINGDEE_EXCHANGERATE_TO_BI_TAG, consumerGroup = RocketMqNewConsumerGroup.DMP_KINGDEE_EXCHANGERATE_TO_BI_GROUP)
public class SyncNewExchangeRateConsumer extends AbstractNewPlatformConsumerHandler {

    @Resource
    private SyncExchangeRateService syncExchangeRateService;

	@Override
	public String getBizName() {
		return "金蝶汇率";
	}

	@Override
	public void handle(String data) {
		syncExchangeRateService.syncKingdeeExchangeRate(JSON.parseObject(data , DmpExchangeRateDTO.class));
	}

}

