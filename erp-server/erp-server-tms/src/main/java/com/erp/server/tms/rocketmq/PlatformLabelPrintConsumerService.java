package com.erp.server.tms.rocketmq;


import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLabelDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.service.LogisticsBillService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
        List<SoB2cLabelDTO.UpdateDTO> dtoList = new ArrayList<>();
        for (SoB2cDTO.WaybillDTO waybillDTO : waybillDTOList) {
            for (String labelBase : waybillDTO.getDistributeBase64Url()) {
                SoB2cLabelDTO.UpdateDTO updateDTO = new SoB2cLabelDTO.UpdateDTO();
                updateDTO.setLogisticsLabelUrl(labelBase);
                updateDTO.setMainId(waybillDTO.getSoB2cId());
                dtoList.add(updateDTO);
            }
        }
        soB2cFeign.saveSoB2cLabel(dtoList);
    }

}
