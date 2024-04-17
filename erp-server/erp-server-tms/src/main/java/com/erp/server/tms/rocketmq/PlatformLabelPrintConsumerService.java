package com.erp.server.tms.rocketmq;


import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.service.LogisticsBillService;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 异步请求存储平台面单
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
        for (SoB2cDTO.WaybillDTO waybillDTO : waybillDTOList) {
            LogisticsBillDTO.SoB2cLabelDTO soB2cLabelDTO = new LogisticsBillDTO.SoB2cLabelDTO();
            soB2cLabelDTO.setSoB2cId(waybillDTO.getSoB2cId());
            soB2cLabelDTO.setLogisticsBase64(StringUtils.join(waybillDTO.getLogisticsBase64(), ","));
            soB2cFeign.updateLogisticsLabelBase64ById(Arrays.asList(soB2cLabelDTO));
        }
    }
}
