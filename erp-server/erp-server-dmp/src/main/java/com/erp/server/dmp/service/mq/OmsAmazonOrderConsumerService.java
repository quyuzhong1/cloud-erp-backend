package com.erp.server.dmp.service.mq;

import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.server.dmp.service.DmpPullTaskService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 平台订单消费服务
 *
 * @Author Cloud
 * @Date 2023/8/31 17:10
 **/
@Service
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC,
        selectorExpression = "demo",
        consumerGroup = "${spring.cloud.nacos.discovery.namespace}-oms_push_amazon_order_consumer")
public class OmsAmazonOrderConsumerService extends AbstractPlatformConsumerHandler<PlatformOrderDTO> {

    @Resource
    private DmpPullTaskService dmpPullTaskService;

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPullTaskService.updateSyncInfo(paramDTO.getDmpSyncTaskId(), paramDTO.getSyncStatus(), paramDTO.getResponseMsg());
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> handle(Object ext) {

        return null;
    }
}
