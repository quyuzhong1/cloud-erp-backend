package com.erp.server.wms.rocketmq.consumer;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.dto.DmpTransferInfoDTO;
import com.erp.server.wms.rocketmq.sync.SyncTransferInfoService;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/20 11:12
 */
@Service
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_KINGDEE_DIRECT_TRANSFER_TO_WMS_TOPIC, 
selectorExpression = RocketMqNewTag.DMP_KINGDEE_DIRECT_TRANSFER_TO_WMS_TAG, 
consumerGroup = RocketMqNewConsumerGroup.DMP_KINGDEE_DIRECT_TRANSFER_TO_WMS_GROUP)
public class SyncNewTransferInfoConsumer extends AbstractNewPlatformConsumerHandler{

	@Resource
    private SyncTransferInfoService syncTransferInfoService;

	@Override
	public String getBizName() {
		return "金蝶调拨";
	}
	
    @Override
	public void handle(String data) {
		DmpTransferInfoDTO dmpTransferInfoDTO = JSON.parseObject(data,  DmpTransferInfoDTO.class);
		syncTransferInfoService.syncKingdeeTransferInfo(dmpTransferInfoDTO);
	}
    
}

