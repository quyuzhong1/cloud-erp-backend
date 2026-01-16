package com.erp.server.wms.rocketmq.consumer;

import com.alibaba.fastjson.JSON;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.wms.dto.AwdInventoryDTO;
import com.erp.server.wms.service.AwdInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * @Author: wtr
 * @Date: 2025/12/26 12:15
 * @Param:
 * @Return:
 * @Description: 下载AWD库存消费服务
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_AWD_INVENTORY_TO_WMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_AWD_INVENTORY_TO_WMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_AWD_INVENTORY_TO_WMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewAwdInventoryConsumerService extends AbstractNewPlatformConsumerHandler {
    @Resource
    private AwdInventoryService awdInventoryService;

    @Override
    public String getBizName() {
        return "AWD库存";
    }

    @Override
    public void handle(String data) {
        AwdInventoryDTO.AddDTO dto = JSON.parseObject(data, AwdInventoryDTO.AddDTO.class);
        awdInventoryService.add(dto);
    }

}