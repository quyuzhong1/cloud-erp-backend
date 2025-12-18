package com.erp.server.wms.rocketmq.consumer;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.PddSoOutStockDTO;
import com.common.business.dto.WdtSoOutStockDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.common.message.handler.AbstractRestCloudPlatformConsumerHandler;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.RESTCLOUD_PDD_SO_OUT_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.RESTCLOUD_PDD_SO_OUT_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_WDT_SO_OUTSTOCK_TO_WMS_GROUP)
public class SyncPddSoOutConsumer extends AbstractRestCloudPlatformConsumerHandler {

	@Resource
    private SyncB2CSoOutstockService syncB2CSoOutstockService;

	@Override
	public String getBizName() {
		return "拼多多出库";
	}
	
	@Override
	public void handle(String data) {
		PddSoOutStockDTO entity = JSON.parseObject(data,  PddSoOutStockDTO.class);
		syncB2CSoOutstockService.syncPddSoOutStock(entity);
	}
}