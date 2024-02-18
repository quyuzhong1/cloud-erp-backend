package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.lingxing.ShopEntity;
import com.erp.server.dmp.service.PlatformApiTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Component
public class MQLingxingConsumerService {

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "lx_shop_info_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_shop_info_consumer")
    public class ConsumerErpShopInfo implements RocketMQListener<ShopEntity> {
        @Override
        public void onMessage(ShopEntity ext) {
            log.info("监听领星店铺信息消息：entity={}", JSONUtil.toJsonStr(ext));
            // 检查任务和记录平台店铺ID
            platformApiTaskService.handleThirdPlatformId(ext);
        }
    }


}
