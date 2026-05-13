package com.erp.server.tms.rocketmq;

import com.common.business.dto.DmpSyncTaskIdDTO;
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
 * 功能描述：快递100 物流轨迹 MQ 消费者
 * <p>
 * 接收来自 DMP 的处理结果，并调用通用轨迹处理服务进行入库
 * </p>
 *
 * @author jack
 * @date 2026-03-31
 */
@Service
@Slf4j
@RocketMQMessageListener(
        topic = RocketMqNewTopic.DMP_KUAIDI100_TO_TMS_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_KUAIDI100_TO_TMS_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_KUAIDI100_TO_TMS_GROUP,
        consumeMode = ConsumeMode.ORDERLY
)
public class Kuaidi100PlatformTrackConsumerService extends AbstractNewPlatformConsumerHandler {

    @Resource
    private PlatformTrackConsumerService<DmpSyncTaskIdDTO> platformTrackConsumerService;

    @Override
    public String getBizName() {
        return "快递100物流轨迹信息";
    }

    @Override
    public void handle(String data) {
        log.info("开始处理快递100物流轨迹消息: {}", data);
        platformTrackConsumerService.handle(data);
    }
}
