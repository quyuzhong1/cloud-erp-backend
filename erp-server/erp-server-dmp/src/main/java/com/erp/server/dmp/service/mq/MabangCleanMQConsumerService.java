package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.DmpDeliveryDetailInfoEntity;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.server.dmp.pull.service.mabang.MabangDeliveryDetailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Cloud
 * 下载数据初步清洗消费者
 */
@Slf4j
@Component
public class MabangCleanMQConsumerService {

    @Resource
    private MabangDeliveryDetailServiceImpl deliveryDetailService;

    /**
     * rocketmq 监听发货订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_DATA_CLEAN_TOPIC,
            selectorExpression = "mabang_delivery_order_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-mabang_clean_delivery_consumer")
    public class ConsumerErpDeliveryOrder implements RocketMQListener<OrderEntity> {
        @Override
        public void onMessage(OrderEntity ext) {
            log.info("监听马帮发货订单清洗消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            deliveryDetailService.addDeliveryOrder(ext);
        }
    }
}
