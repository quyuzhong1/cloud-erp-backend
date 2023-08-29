package com.erp.server.oms.rocketmq;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.PlatformOrderDataDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.SalesPlatformEnum;
import com.common.business.handler.AbstractOrderHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * Amazon订单处理器
 * @author Cloud
 */

@Slf4j
@Service
@PlatformCategoryType(PlatformCategoryEnum.OMS)
@PlatformType(SalesPlatformEnum.AMAZON)
@BusinessType(BusinessTypeEnum.ORDER)
@RocketMQMessageListener(topic = "demo", consumerGroup = "demo", selectorExpression = "", consumeMode = ConsumeMode.ORDERLY)
public class AmazonOrderHandler extends AbstractOrderHandler<PlatformOrderDataDTO> implements RocketMQListener<PlatformOrderDataDTO> {

    @Override
    public void onMessage(PlatformOrderDataDTO message) {
        // TODO 如果失败重试消费去掉try catch 并修改consumeMode为ConsumeMode.CONCURRENTLY 并发消费
        //  避免使用ConsumeMode.ORDERLY顺序消费模式的同时使用报错重试，会导致消息消费失败一直重试并阻塞后续消费
        try {
            handle(message);
        }catch (Exception e){
            log.error("AmazonOrderHandler onMessage error", e);
        }
    }

    @Override
    public void handle(PlatformOrderDataDTO message) {
        // 1. Deserialize the message body to PlatformOrderDataDTO


        // 2. Specific logic for Amazon order handling using data
    }
}
