package com.erp.server.dmp.push.consumer.erp;

import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

/**
 * ERP的b2c订单推送到金蝶消费者
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_SO_B2C_DELIVERY_TO_DMP_TOPIC, selectorExpression = "so_b2c_delivery_to_dmp_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_ERP_SO_B2C_DELIVERY_TO_DMP,
        consumeMode = ConsumeMode.ORDERLY)
public class B2cDeliveryPushDmpDeliveryConsumer extends AbstractPlatformConsumerHandler<DmpSyncMqDTO> {

    @Override
    public void updateSyncTaskStatus(String syncTaskId, SyncStatusEnum code, String msg) {

    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {

    }

    @Override
    public ApiResult<?> handle(Object ext) {
        return null;
    }
}
