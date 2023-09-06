package com.erp.server.dmp.push.consumer.amz;


import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.common.business.dto.DmpSyncMqDTO;
import com.erp.sdk.oms.amz.spapi.service.PushOrderService;
import com.erp.server.dmp.service.DmpPullTaskService;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import javax.annotation.Resource;

/**
 * 亚马逊订单推送消费者
 *
 * @Author Cloud
 * @Date 2023/9/4 10:19
 **/
@RocketMQMessageListener(topic = RocketMqTopic.PLATFORM_PUSH_DATA_TOPIC, selectorExpression = "OMS_amazon_order_tag",
        consumerGroup = RocketMqConsumerGroup.SYNC_AMAZON_ORDER_FROM_OMS,
        consumeMode = ConsumeMode.ORDERLY)
public class AmazonOrderPushConsumer extends AbstractPlatformConsumerHandler<DmpSyncMqDTO> {

    @Resource
    private PushOrderService pushOrderService;
    @Resource
    private DmpPullTaskService dmpPullTaskService;


    @Override
    public void updateSyncTaskStatus(String id, SyncStatusEnum code, String msg) {
        dmpPullTaskService.updateSyncInfo(id, code.getCode(), msg);
    }

    @Override
    public ApiResult handle(DmpSyncMqDTO ext) {
        // 调用亚马逊订单推送服务
        return pushOrderService.handle(ext);
    }
}
