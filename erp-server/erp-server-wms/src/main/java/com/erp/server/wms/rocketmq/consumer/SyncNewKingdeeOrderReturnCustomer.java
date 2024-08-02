package com.erp.server.wms.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.server.wms.rocketmq.sync.SyncSoReturnService;



@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_KINGDEE_ORDER_RETURN_TO_WMS_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_KINGDEE_ORDER_RETURN_TO_WMS_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_KINGDEE_ORDER_RETURN_TO_WMS_GROUP)
public class SyncNewKingdeeOrderReturnCustomer extends AbstractNewPlatformConsumerHandler{

    @Resource
    private SyncSoReturnService syncSoReturnService;

    @Override
	public String getBizName() {
		return "金蝶退货";
	}
    
    @Override
	public void handle(String data) {
		KingdeeReturnOrderEntity entity = JSON.parseObject(data,  KingdeeReturnOrderEntity.class);
		syncSoReturnService.syncKingdeeReturnOrderToSoReturn(entity);
	}
    
}
