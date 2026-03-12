package com.erp.server.wms.service.impl;

import com.common.business.dto.PlatformOutboundDTO;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.service.mq.MQProducerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * B2B三方仓出库状态消息生产者
 */
@Service
public class B2bThirdOutboundMqProducer {

    @Resource
    private MQProducerService<PlatformOutboundDTO> mqProducerService;

    public void send(PlatformOutboundDTO dto) {
        mqProducerService.syncClassMsg(
                RocketMqNewTopic.DMP_PLATFORM_B2B_THIRD_OUTBOUND_TO_WMS_TOPIC,
                RocketMqNewTag.DMP_PLATFORM_B2B_THIRD_OUTBOUND_TO_WMS_TAG,
                dto,
                dto.getReferenceNo()
        );
    }
}
