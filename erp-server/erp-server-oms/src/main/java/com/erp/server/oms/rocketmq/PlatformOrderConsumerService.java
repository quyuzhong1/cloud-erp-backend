package com.erp.server.oms.rocketmq;

import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.dto.UniqueDto;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 下载平台订单消费服务
 * @author Cloud
 */
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "third_system_Amazon_order_tag||third_system_Shopify_listing_tag",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-platform_pull_amazon_order_consumer",
        consumeMode = ConsumeMode.ORDERLY)
public class PlatformOrderConsumerService<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void updateSyncTaskStatus(String id, SyncKingdeeStatusEnum code, String msg) {
        dmpTaskFeign.updateSyncInfo(new DmpSyncMqDTO.ParamDTO(id, code.getCode(), msg));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult handle(T ext) {

        return null;
    }
}
