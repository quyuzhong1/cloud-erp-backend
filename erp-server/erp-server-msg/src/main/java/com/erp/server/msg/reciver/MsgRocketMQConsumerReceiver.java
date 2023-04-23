package com.erp.server.msg.reciver;

import com.alibaba.fastjson.JSONObject;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.server.msg.config.MsgContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Classname: RocketMQConsumerReceiver
 * @Description: RocketMQ消费者监听
 * @CreateTime: 2023-04-21  09:53
 * @Author: zhangchunlin
 */
@Slf4j
@Component
public class MsgRocketMQConsumerReceiver {

    @Resource
    private MsgContext msgContext;

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.NOTICE_MSG_TOPIC,
            consumerGroup = "${spring.profiles.active}-erp_msg_group")
    public class ConsumerNoticeMsg implements RocketMQListener<NoticeMsgInfoDTO> {
        @Override
        public void onMessage(NoticeMsgInfoDTO msgInfoDTO) {
            log.info("监听到消息发送消息通知，请求内容：{}", JSONObject.toJSONString(msgInfoDTO));
            // 此处需注意，如果内部抛异常可能会导致某个渠道发送正常重新发送
            msgContext.routeSend(msgInfoDTO);
        }
    }

}