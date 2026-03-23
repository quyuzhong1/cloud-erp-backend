package com.erp.server.dmp.service.mq;

import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@RocketMQMessageListener(
        topic = RocketMqNewTopic.DMP_TIKTOK_WAREHOUSE_TO_DMP_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_TIKTOK_WAREHOUSE_TO_DMP_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_TIKTOK_WAREHOUSE_TO_DMP_GROUP
)
public class NewTikTokWarehouseConsumer extends AbstractNewPlatformConsumerHandler {

    @Resource
    private TikTokWarehouseConsumer tikTokWarehouseConsumer;

    @Override
    public String getBizName() {
        return "TikTok销售仓库";
    }

    @Override
    public void handle(String data) {
        ApiResult<?> result = tikTokWarehouseConsumer.handle(data);
        if (result == null || !result.isSuccess()) {
            throw new ServiceException(result == null ? "TikTok销售仓库消费失败" : result.getMsg());
        }
    }
}
