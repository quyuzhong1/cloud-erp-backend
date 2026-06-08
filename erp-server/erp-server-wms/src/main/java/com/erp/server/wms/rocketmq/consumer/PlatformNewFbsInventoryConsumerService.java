package com.erp.server.wms.rocketmq.consumer;

import com.alibaba.fastjson.JSON;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.wms.dto.FbsInventoryDTO;
import com.erp.server.wms.service.FbsInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * FBS 库存 MQ 消费
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FBS_INVENTORY_TO_WMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FBS_INVENTORY_TO_WMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FBS_INVENTORY_TO_WMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewFbsInventoryConsumerService extends AbstractNewPlatformConsumerHandler {

    @Resource
    private FbsInventoryService fbsInventoryService;

    @Override
    public String getBizName() {
        return "FBS库存";
    }

    @Override
    public void handle(String data) {
        FbsInventoryDTO.AddDTO dto = JSON.parseObject(data, FbsInventoryDTO.AddDTO.class);
        fbsInventoryService.add(dto);
    }
}
