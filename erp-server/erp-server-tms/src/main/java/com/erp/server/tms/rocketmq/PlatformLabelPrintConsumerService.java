package com.erp.server.tms.rocketmq;


import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.service.LogisticsBillService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 报告步骤1
 * 报告创建消费处理
 */
@Service
@RocketMQMessageListener(topic = RocketMqTopic.ASYNC_GET_PLATFORM_LABEL_TOPIC,
        selectorExpression = "async_get_platform_label_tag",
        consumerGroup = RocketMqConsumerGroup.ASYNC_GET_PLATFORM_LABEL_CONSUMER)
public class PlatformLabelPrintConsumerService implements RocketMQListener<LogisticsBillDTO.PrintLogisticsWaybillDTO> {
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private SoB2cFeign soB2cFeign;

    @Override
    public void onMessage(LogisticsBillDTO.PrintLogisticsWaybillDTO dto) {
        List<SoB2cDTO.WaybillDTO> waybillDTOList = logisticsBillService.printLogisticsWaybill(Arrays.asList(dto));

        SoB2cEntity soB2cEntity = soB2cFeign.getById(dto.getB2cSoId());

        for (SoB2cDTO.WaybillDTO waybillDTO : waybillDTOList) {
            soB2cEntity.setLogisticsLabelBase64(waybillDTO.getDistributeBase64());
            soB2cFeign.updateById(soB2cEntity);
        }
    }
}
