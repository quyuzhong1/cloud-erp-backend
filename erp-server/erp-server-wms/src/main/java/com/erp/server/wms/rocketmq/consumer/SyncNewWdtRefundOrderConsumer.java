package com.erp.server.wms.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.WdtReturnOrderDTO;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.server.wms.rocketmq.sync.SyncSoReturnService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_WDT_ORDER_RETURN_TO_WMS_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_WDT_ORDER_RETURN_TO_WMS_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_WDT_ORDER_RETURN_TO_WMS_GROUP)
public class SyncNewWdtRefundOrderConsumer extends AbstractNewPlatformConsumerHandler {

    @Resource
    private SyncSoReturnService syncSoReturnService;

	@Override
	public void handle(String data) {
		log.warn("新中台处理旺店通退货数据：{}" , data);
		WdtReturnOrderDTO entity = JSON.parseObject(data,  WdtReturnOrderDTO.class);
		syncSoReturnService.syncWdtReturnOrderToSoReturn(entity);
	}
}