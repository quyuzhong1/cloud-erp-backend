package com.erp.server.dmp.push.consumer.amz;


import com.common.business.dto.DmpSyncMqDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.sdk.oms.amz.spapi.service.PushOrderService;
import com.erp.server.dmp.service.DmpPushTaskService;
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
    private DmpPushTaskService dmpPushTaskService;


    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPushTaskService.updateStatus(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {

    }

    @Override
    public ApiResult<?> handle(Object ext) {
        // 调用亚马逊订单推送服务
        return pushOrderService.handle(ext);
    }
}
