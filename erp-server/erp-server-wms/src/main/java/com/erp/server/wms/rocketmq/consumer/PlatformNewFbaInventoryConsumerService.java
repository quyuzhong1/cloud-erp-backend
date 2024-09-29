package com.erp.server.wms.rocketmq.consumer;

import com.alibaba.fastjson.JSON;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.server.wms.service.FbaInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 下载FBA库存消费服务
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FBA_INVENTORY_TO_WMS_TOPIC,
selectorExpression = RocketMqNewTag.DMP_FBA_INVENTORY_TO_WMS_TAG,
consumerGroup = RocketMqNewConsumerGroup.DMP_FBA_INVENTORY_TO_WMS_GROUP,
consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewFbaInventoryConsumerService extends AbstractNewPlatformConsumerHandler {
	@Resource
	private FbaInventoryService fbaInventoryService;
	
	@Override
	public String getBizName() {
		return "FBA库存";
	}

	@Override
	public void handle(String data) {
//		FbaInventoryEntity dto = JSONUtil.toBean(data, FbaInventoryEntity.class);
		FbaInventoryEntity dto = JSON.parseObject(data, FbaInventoryEntity.class);
		fbaInventoryService.allBatchSave(Collections.singletonList(dto));
	}

}