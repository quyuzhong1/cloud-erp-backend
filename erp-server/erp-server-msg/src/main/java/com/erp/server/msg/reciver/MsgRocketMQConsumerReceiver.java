package com.erp.server.msg.reciver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.IdUtils;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.server.msg.config.MsgContext;
import com.erp.server.msg.constant.MongoTableConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

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
    @Resource
    private MongoTemplate mongoTemplate;
    /**
     * 是否启用xxl-job 进行定时发送
     */
    @Value("${warn_msg_use_job}")
    private Boolean useJob;
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.NOTICE_MSG_TOPIC,
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-erp_msg_group")
    public class ConsumerNoticeMsg implements RocketMQListener<NoticeMsgInfoDTO> {
        @Override
        public void onMessage(NoticeMsgInfoDTO msgInfoDTO) {
            log.info("监听到消息发送消息通知，请求内容：{}", JSON.toJSONString(msgInfoDTO));
            // 此处需注意，如果内部抛异常可能会导致某个渠道发送正常重新发送
            msgContext.routeSend(msgInfoDTO);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.WARN_MSG_TOPIC,
            selectorExpression = "msg_warn_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-erp_warn_group")
    public class ConsumerWarnMsg implements RocketMQListener<WarnMsgInfoDTO> {
        @Override
        public void onMessage(WarnMsgInfoDTO warnMsgInfo) {
            log.info("监听到消息发送预警消息通知，请求内容：{}", JSON.toJSONString(warnMsgInfo));
            //判断是否开启消息异步管理
            msgContext.routeSendWarnMsg(warnMsgInfo);
        }
    }

}