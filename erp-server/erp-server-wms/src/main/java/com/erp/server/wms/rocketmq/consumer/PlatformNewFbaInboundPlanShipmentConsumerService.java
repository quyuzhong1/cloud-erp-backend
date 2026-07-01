package com.erp.server.wms.rocketmq.consumer;

import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * FBA InboundPlan 货件消费服务（复用 FBA 货件消费逻辑）
 * <p>
 * 审查问题4（intentional）：幂等不依赖本类 {@code @DataIdempotent}，与 {@link PlatformNewFbaShipmentConsumerService} 相同——
 * 父类 {@link com.common.message.handler.AbstractNewPlatformConsumerHandler} 对 {@code dmpOutputTaskRecordDataId}
 * 加 Redisson 分布式锁；委托 {@link PlatformFbaShipmentConsumerService#handle} 按 {@code fbaShipmentId} upsert，重复消费为更新而非重复插入。
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_FBA_SHIPMENT_TO_WMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_FBA_INBOUND_PLAN_TO_WMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_FBA_INBOUND_PLAN_TO_WMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformNewFbaInboundPlanShipmentConsumerService extends AbstractNewPlatformConsumerHandler {

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
