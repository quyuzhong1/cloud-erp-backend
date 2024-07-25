package com.erp.server.wms.rocketmq.consumer;



import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.server.wms.rocketmq.sync.SyncB2CSoOutstockService;

import lombok.extern.slf4j.Slf4j;

/**
 * B2C 销售出库 金蝶同步到WMS
 *
 * @author Lambda
 * @Classname KingdeeB2CSoOutstockConsumer
 * @Date 2023-06-27 10:21
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_KINGDEE_SO_OUTSTOCK_TO_WMS_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_KINGDEE_SO_OUTSTOCK_TO_WMS_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_KINGDEE_SO_OUTSTOCK_TO_WMS_GROUP)
public class KingdeeNewB2CSoOutstockConsumer extends AbstractNewPlatformConsumerHandler{

    @Resource
    private SyncB2CSoOutstockService syncB2CSoOutstockService;

    @Override
	public void handle(String data) {
		log.warn("新中台处理金蝶出库数据：{}" , data);
		KingdeeDeliveryDetailEntity entity = JSON.parseObject(data,  KingdeeDeliveryDetailEntity.class);
		syncB2CSoOutstockService.syncKingdeeSoOutstock(entity);
	}
    
}
