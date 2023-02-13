package com.erp.server.dmp.service.mq;

import com.common.core.constant.RocketMqTopic;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
//    @Service
//    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC, selectorExpression = "tag2", consumerGroup = "Con_Group_Two", consumeMode = ConsumeMode.ORDERLY)
//    public class ConsumerSend2 implements RocketMQListener<String> {
//        @Override
//        public void onMessage(String str) {
//            log.info("监听到消息：str={}", str);
//        }
//    }

    // MessageExt：是一个消息接收通配符，不管发送的是String还是对象，都可接收，当然也可以像上面明确指定类型（我建议还是指定类型较方便）
    /**
     * consumeMode = ConsumeMode.CONCURRENTLY  集群模式 重试16次  默认此模式
     * consumeMode = ConsumeMode.ORDERLY  每秒进行一次重试 一直重试
     */
//    @Service
//    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC, selectorExpression = "tag1", consumerGroup = "Con_Group_Three", consumeMode = ConsumeMode.CONCURRENTLY)
//    public class Consumer implements RocketMQListener<MessageExt> {
//        @Override
//        public void onMessage(MessageExt messageExt) {
//            byte[] body = messageExt.getBody();
//            String msg = new String(body);
//            log.info("监听到消息：msg={}", msg);
//            throw new RuntimeException("tag1 失败测试");
//        }
//    }
}
