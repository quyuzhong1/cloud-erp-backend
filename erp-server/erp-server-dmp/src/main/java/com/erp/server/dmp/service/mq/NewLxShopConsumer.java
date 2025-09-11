package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.common.message.handler.AbstractNewPlatformConsumerHandler;
import com.erp.model.dmp.lingxing.ShopEntity;
import com.erp.server.dmp.service.ShopInfoMappingService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 领星店铺消费
 */
@Component
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_LX_SHOP_TO_DMP_TOPIC,
        selectorExpression = RocketMqNewTag.DMP_LX_SHOP_TO_DMP_TAG,
        consumerGroup = RocketMqNewConsumerGroup.DMP_LX_SHOP_TO_DMP_GROUP)
public class NewLxShopConsumer extends AbstractNewPlatformConsumerHandler {

    @Resource
    private ShopInfoMappingService shopInfoMappingService;

    @Override
    public String getBizName() {
        return "领星店铺";
    }

    @Override
    public void handle(String data) {
        ShopEntity ext = JSONUtil.toBean(data, ShopEntity.class);
        // 检查任务和记录平台店铺ID
        shopInfoMappingService.saveAndHandle(ext);
    }
}