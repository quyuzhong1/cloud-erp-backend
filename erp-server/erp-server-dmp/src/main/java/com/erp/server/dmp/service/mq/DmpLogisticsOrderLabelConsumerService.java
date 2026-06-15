package com.erp.server.dmp.service.mq;

import cn.hutool.core.util.ObjectUtil;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * DMP异步请求存储物流下单面单
 */
@Service
@RocketMQMessageListener(topic = RocketMqTopic.DMP_ASYNC_GET_LOGISTICS_ORDER_LABEL_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_ASYNC_GET_LOGISTICS_ORDER_LABEL_TAG,
        consumerGroup = RocketMqConsumerGroup.DMP_ASYNC_GET_LOGISTICS_ORDER_LABEL_CONSUMER)
@Slf4j
public class DmpLogisticsOrderLabelConsumerService implements RocketMQListener<List<LogisticsOrderDTO.LogisticsLabelDTO>> {

    @Resource
    private AfterSaleService afterSaleService;

    @Override
    public void onMessage(List<LogisticsOrderDTO.LogisticsLabelDTO> logisticsLabelDTOS) {
        log.warn("接收到批量异步请求打印物流下单面单消息，数量：{}", logisticsLabelDTOS.size());
        logisticsLabelDTOS.forEach(dto -> dto.setIsFromMq(true));
        List<String> afterSaleIds = logisticsLabelDTOS.stream().map(LogisticsOrderDTO.LogisticsLabelDTO::getAfterSaleId).filter(ObjectUtil::isNotEmpty).distinct().collect(Collectors.toList());
        afterSaleService.getLogisticsOrderLabel(afterSaleIds, logisticsLabelDTOS);
    }
}
