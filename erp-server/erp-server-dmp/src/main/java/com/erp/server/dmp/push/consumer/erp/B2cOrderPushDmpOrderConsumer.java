package com.erp.server.dmp.push.consumer.erp;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

/**
 * ERP的b2c订单推送到金蝶消费者
 */
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_SO_B2C_ORDER_TO_DMP_TOPIC, selectorExpression = "so_b2c_to_dmp_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_ERP_SO_B2C_TO_DMP,
        consumeMode = ConsumeMode.ORDERLY)
public class B2cOrderPushDmpOrderConsumer {

}
