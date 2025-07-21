package com.erp.server.wms.rocketmq.consumer;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.TeMuSoOutStockDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_TEMU_SO_OUTSTOCK_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_TEMU_SO_OUTSTOCK_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_TEMU_SO_OUTSTOCK_TO_WMS_GROUP)
public class SyncNewTeMuOutStockConsumer extends AbstractNewPlatformConsumerHandler {

	@Resource
    private SyncB2CSoOutstockService syncB2CSoOutstockService;

	@Override
	public String getBizName() {
		return "temu平台仓出库";
	}
	
	@Override
	public void handle(String data) {
		TeMuSoOutStockDTO entity = JSON.parseObject(data,  TeMuSoOutStockDTO.class);
		syncB2CSoOutstockService.syncTemuSoOutStock(entity);
	}
}