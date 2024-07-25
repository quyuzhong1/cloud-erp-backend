package com.erp.server.wms.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.WdtSoOutStockDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_WDT_SO_OUTSTOCK_TO_WMS_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_WDT_SO_OUTSTOCK_TO_WMS_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_WDT_SO_OUTSTOCK_TO_WMS_GROUP)
public class SyncNewWdtDeliveryConsumer extends AbstractNewPlatformConsumerHandler {

	@Resource
    private SyncB2CSoOutstockService syncB2CSoOutstockService;

	@Override
	public void handle(String data) {
		log.warn("新中台处理旺店通出库数据：{}" , data);
		WdtSoOutStockDTO entity = JSON.parseObject(data,  WdtSoOutStockDTO.class);
		syncB2CSoOutstockService.syncWdtSoOutStock(entity);
	}
}