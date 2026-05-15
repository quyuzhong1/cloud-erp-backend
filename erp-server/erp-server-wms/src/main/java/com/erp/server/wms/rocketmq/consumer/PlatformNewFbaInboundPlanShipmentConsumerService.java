package com.erp.server.wms.rocketmq.consumer;

import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * FBA InboundPlan 货件消费服务（复用FBA货件消费逻辑）
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FBA_SHIPMENT_TO_WMS_TOPIC,
        selectorExpression = PlatformNewFbaInboundPlanShipmentConsumerService.DMP_FBA_INBOUND_PLAN_TO_WMS_TAG,
        consumerGroup = PlatformNewFbaInboundPlanShipmentConsumerService.DMP_FBA_INBOUND_PLAN_TO_WMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewFbaInboundPlanShipmentConsumerService extends AbstractNewPlatformConsumerHandler {

    /**
     * 使用新tag隔离老FBA货件消费者，避免重复消费
     */
    public static final String DMP_FBA_INBOUND_PLAN_TO_WMS_TAG = "${spring.cloud.nacos.discovery.namespace}-dmp_fba_inbound_plan_to_wms_tag";

    /**
     * 使用独立group消费 InboundPlan 货件消息
     */
    public static final String DMP_FBA_INBOUND_PLAN_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_fba_inbound_plan_to_wms_group";

    @Resource
    private PlatformFbaShipmentConsumerService platformFbaShipmentConsumerService;

    @Override
    public String getBizName() {
        return "FBA入库计划货件";
    }

    @Override
    public void handle(String data) {
        platformFbaShipmentConsumerService.handle(data);
    }
}
