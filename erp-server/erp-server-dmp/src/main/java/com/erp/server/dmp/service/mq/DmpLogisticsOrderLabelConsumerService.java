package com.erp.server.dmp.service.mq;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.server.dmp.service.AfterSaleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * DMP异步请求存储物流下单面单
 */
@Service
@RocketMQMessageListener(topic = RocketMqTopic.DMP_ASYNC_GET_LOGISTICS_ORDER_LABEL_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_ASYNC_GET_LOGISTICS_ORDER_LABEL_TAG,
        consumerGroup = RocketMqConsumerGroup.DMP_ASYNC_GET_LOGISTICS_ORDER_LABEL_CONSUMER)
@Slf4j
public class DmpLogisticsOrderLabelConsumerService implements RocketMQListener<LogisticsOrderDTO.LogisticsLabelDTO> {

    @Resource
    private AfterSaleService afterSaleService;

    @Override
    public void onMessage(LogisticsOrderDTO.LogisticsLabelDTO logisticsLabelDTO) {
        log.warn("接收到异步请求打印物流下单面单消息：{}", logisticsLabelDTO);
        logisticsLabelDTO.setIsFromMq(true);
        afterSaleService.getLogisticsOrderLabel(Collections.singletonList(logisticsLabelDTO));
    }
}
