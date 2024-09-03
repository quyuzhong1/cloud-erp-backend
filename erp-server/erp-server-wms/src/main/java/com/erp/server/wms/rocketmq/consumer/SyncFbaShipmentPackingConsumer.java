package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.erp.server.wms.service.FbaShipmentPackingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "fba_shipment_packing_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-fba_shipment_packing_tag_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class SyncFbaShipmentPackingConsumer extends AbstractNewPlatformConsumerHandler {
    @Resource
    private FbaShipmentPackingService fbaShipmentPackingService;

    @Override
    public String getBizName() {
        return "fba货件装箱";
    }

    @Override
    public void handle(String data) {
        FbaShipmentPackingEntity dto = JSONUtil.toBean(data, FbaShipmentPackingEntity.class);
        fbaShipmentPackingService.handle(dto);
    }
}
