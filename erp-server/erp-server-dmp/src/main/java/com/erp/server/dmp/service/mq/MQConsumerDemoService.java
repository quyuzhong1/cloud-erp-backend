package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.erp.common.business.constant.RocketMqTopic;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消费demo
 *
 * @Author Cloud
 * @Date 2023/2/10 9:16
 **/
@Slf4j
@Component
public class MQConsumerDemoService {

    /**
     * consumeMode = ConsumeMode.ORDERLY  每秒进行一次重试 一直重试
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_CONSUMER_TOPIC, selectorExpression = "tag2", consumerGroup = "tag2_consumer", consumeMode = ConsumeMode.ORDERLY)
    public class ConsumerSend2 implements RocketMQListener<String> {
        @Override
        public void onMessage(String str) {
            log.info("监听到消息：str={}", str);
        }
    }

    // MessageExt：是一个消息接收通配符，不管发送的是String还是对象，都可接收，当然也可以像上面明确指定类型（我建议还是指定类型较方便）
    /**
     * consumeMode = ConsumeMode.CONCURRENTLY  集群模式 重试16次  默认此模式
     * consumeMode = ConsumeMode.ORDERLY  每秒进行一次重试 一直重试
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_CONSUMER_TOPIC, selectorExpression = "tag1", consumerGroup = "tag1_consumer")
    public class Consumer implements RocketMQListener<ProducerDto.EntityDto> {
        @Override
        public void onMessage(ProducerDto.EntityDto dto) {
            log.info("监听到消息：msg={}", JSONUtil.toJsonStr(dto));
        }
    }


    @Data
    public static class ProducerDto{

        private String tag;

        private EntityDto entity;

        @Data
        public static class EntityDto{

            private Integer id;

            private LocalDateTime createTime;

            private String name;
        }

    }

}
