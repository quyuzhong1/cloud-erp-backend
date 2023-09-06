package com.erp.server.dmp.service.mq;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.server.dmp.service.DmpSyncTaskService;
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
    private DmpSyncTaskService dmpSyncTaskService;

    @Override
    public void updateSyncTaskStatus(String id, SyncKingdeeStatusEnum code, String msg) {
        dmpSyncTaskService.updateSyncInfo(id, code.getCode(), msg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult handle(PlatformOrderDTO ext) {

        return null;
    }
}
