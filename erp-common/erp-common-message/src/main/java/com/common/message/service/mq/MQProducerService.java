package com.common.message.service.mq;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.utils.IdUtils;
import com.common.core.utils.ValidatorUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.entity.MessageBody;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.NoticeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MQProducerService<T> {
    private enum MSG_TYPE{ ONEWAY, ASYNC, SYNC };

    /**
     * 直接注入使用，用于发送消息到broker服务器
     */
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");


	private void sendMsg(MSG_TYPE msgType,String msgKey, String destination, Object payload, String msgSource){
        if(CharSequenceUtil.isBlank(msgKey)){
            msgKey = IdUtils.simpleUUID();
        }
        MessageBody msgBody = new MessageBody(msgKey, payload , msgSource);
        Message<MessageBody> message = MessageBuilder.withPayload(msgBody).build();
        log.info("消息发送 MQService 开始: {} {}", destination, message);
        SendResult result = null;
        switch (msgType) {
            case ONEWAY:
                rocketMQTemplate.sendOneWay(destination, message);
                break;
            case ASYNC:
                rocketMQTemplate.asyncSend(destination, message,new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                    }
                    @Override
                    public void onException(Throwable throwable) {
                        log.error("MQService:" + ExceptionUtils.getStackTrace(throwable));
                        throw new RuntimeException(CharSequenceUtil.format("消息发送失败 topic_tag={}", destination ));
                    }
                });
                break;
            case SYNC:
                result = rocketMQTemplate.syncSend(destination, message);
                break;
            default:
                break;
        }
        log.info("消息发送 MQService 结束: result: {} dest: {} msg: {}}", JSONUtil.toJsonStr(result), destination, message);
    }

    /**
     * 同步发送消息,会确认应答
     * @param destination
     * @param payload
     */
    private void syncSendMsg(String msgKey, String destination, Object payload, String msgSource){
        sendMsg(MSG_TYPE.SYNC, msgKey, destination, payload, msgSource) ;
    }
    /**
     * 同步发送消息,会确认应答
     * @param topic
     * @param tag
     * @param payload
     */
    public void syncSendMsg(String msgKey, String topic,String tag, Object payload, String msgSource){
        // 发送的消息体，消息体必须存在
        // 业务主键作为消息key
        String destination = CharSequenceUtil.format("{}:{}", topic.replace("${spring.cloud.nacos.discovery.namespace}", namespace), tag);
        syncSendMsg(msgKey, destination, payload, msgSource);
    }
    /**
     * 异步消息发送,异步日志确认异常
     * @param destination
     * @param payload
     */
    public void asyncSendMsg(String msgKey, String destination, Object payload, String msgSource){
        sendMsg(MSG_TYPE.ASYNC, msgKey,destination, payload,msgSource);
    }

    /**
     * 单向发送消息，不关注结果
     * @param destination
     * @param payload
     */
    private void oneWaySendMsg(String msgKey, String destination, Object payload, String msgSource){
        sendMsg(MSG_TYPE.ONEWAY, msgKey,destination, payload, msgSource);
    }
    /**
     * 单向发送消息，不关注结果
     * @param topic
     * @param tag
     * @param payload
     */
    public void oneWaySendMsg(String msgKey,String topic, String tag, Object payload, String msgSource){
        // 发送的消息体，消息体必须存在
        // 业务主键作为消息key
        String destination = CharSequenceUtil.format("{}:{}", topic.replace("${spring.cloud.nacos.discovery.namespace}", namespace), tag);
        oneWaySendMsg(msgKey, destination, payload,msgSource);
    }


    /**
     *发送批量消息
     * @param topic
     * @param tag
     * @param msgs
     * @return
     */
    public SendResult sendBachMsg(String topic, String tag, List<T> msgs) {
        List<Message<T>> messageList = msgs.stream()
                .map(msg -> MessageBuilder.withPayload(msg)
                        .setHeader(RocketMQHeaders.KEYS, IdUtil.getSnowflake())
                        .build())
                .collect(Collectors.toList());
        return rocketMQTemplate.syncSend(CharSequenceUtil.format("{}:{}", topic.replace("${spring.cloud.nacos.discovery.namespace}", namespace), tag), messageList);
    }

    /**
     * 同步发送对象消息
     * @param topic
     * @param tag
     * @param entity
     * @param key
     * @return
     */
    public SendResult syncClassMsg(String topic, String tag, T entity, String key) {
        Message<T> msg = MessageBuilder.withPayload(entity)
                .setHeader(RocketMQHeaders.KEYS, key)
                .build();
        return rocketMQTemplate.syncSend(CharSequenceUtil.format("{}:{}", topic.replace("${spring.cloud.nacos.discovery.namespace}", namespace), tag), msg);
    }

    /**
     * 同步发送对象消息
     * @param topic
     * @param tag
     * @param entity
     * @param key
     * @return
     */
    public SendResult syncClassMsgByDelayLevel(String topic, String tag, T entity, String key) {
        Message<T> msg = MessageBuilder.withPayload(entity)
                .setHeader(RocketMQHeaders.KEYS, key)
                .build();

        return rocketMQTemplate.syncSend(CharSequenceUtil.format("{}:{}", topic.replace("${spring.cloud.nacos.discovery.namespace}", namespace), tag), msg,3000, 6);
    }

    /**
     * 指定延时等级同步发送对象消息
     * @param topic
     * @param tag
     * @param entity
     * @param key
     * @return
     */
    public SendResult syncClassMsgWithDelayLevel(String topic, String tag, T entity, String key, int delayLevel) {
        Message<T> msg = MessageBuilder.withPayload(entity)
                .setHeader(RocketMQHeaders.KEYS, key)
                .build();

        return rocketMQTemplate.syncSend(CharSequenceUtil.format("{}:{}", topic.replace("${spring.cloud.nacos.discovery.namespace}", namespace), tag), msg,3000, delayLevel);
    }


    /**
     * 异步发送对象消息
     * @param topic
     * @param tag
     * @param entity
     * @param key
     */
    public void asyncClassMsg(String topic, String tag, T entity, String key) {
        Message<T> msg = MessageBuilder.withPayload(entity)
                .setHeader(RocketMQHeaders.KEYS, key)
                .build();
        String destination = CharSequenceUtil.format("{}:{}", topic.replace("${spring.cloud.nacos.discovery.namespace}", namespace), tag);
        rocketMQTemplate.asyncSend(destination, msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("MQService:destination={} entity={} e={}",destination, JSONUtil.toJsonStr(entity), ExceptionUtils.getStackTrace(throwable));
                throw new RuntimeException(String.format("消息发送失败 topic_tag:%s", destination));
            }
        });
    }

    /**
     * 往消息中心发送MQ任务消息
     * @param msgInfoDTO
     * @param isSync（true为同步，其他则为异步）
     * @return 同步时返回，异步返回null
     */
    public SendResult sendNoticeMsg(NoticeMsgInfoDTO msgInfoDTO, Boolean isSync) {
        ValidatorUtil.validateEntity(msgInfoDTO);
        String key = IdUtil.simpleUUID();
        Message<NoticeMsgInfoDTO> msg = MessageBuilder.withPayload(msgInfoDTO)
                .setHeader(RocketMQHeaders.KEYS, key)
                .build();

        NoticeTypeEnum noticeTypeEnum = msgInfoDTO.getNoticeTypeEnum();
        String topic = RocketMqTopic.NOTICE_MSG_TOPIC.replace("${spring.cloud.nacos.discovery.namespace}", namespace);
        String destination = CharSequenceUtil.format("{}:{}", topic , noticeTypeEnum.getMqTag());

        if(Objects.equals(Boolean.TRUE, isSync)) {
            return rocketMQTemplate.syncSend(destination, msgInfoDTO);
        } else {
            asyncClassMsg(topic, noticeTypeEnum.getMqTag(), (T) msgInfoDTO, key);
            return null;
        }
    }

    /**
     * 往消息中心发送同步MQ消息
     * @param msgInfoDTO
     * @return 同步时返回，异步返回null
     */
    public SendResult sendNoticeMsg(NoticeMsgInfoDTO msgInfoDTO) {
        return sendNoticeMsg(msgInfoDTO, Boolean.TRUE);
    }

    /**
     * 往消息中心发送MQ预警消息
     * @param msgInfoDTO
     */
    public void sendWarnMsg(WarnMsgInfoDTO msgInfoDTO) {
        ValidatorUtil.validateEntity(msgInfoDTO);
        String key = IdUtil.simpleUUID();
        msgInfoDTO.setHappenTime(LocalDateTime.now());
        String topic = RocketMqTopic.WARN_MSG_TOPIC.replace("${spring.cloud.nacos.discovery.namespace}", namespace);
        try {
            asyncClassMsg(topic, RocketMqTagEnum.MSG_WARN_TAG.getName(), (T) msgInfoDTO, key);
        } catch (Exception e) {
            log.error("异步发送MQ消息异常", e);
        }
    }

    // RocketMQ默认延时等级和对应的延时时间（秒）
    private static final List<Integer> DELAY_LEVELS = Arrays.asList(1, 5, 10, 30, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 1200, 1800, 3600, 7200);

    /**
     * 将秒数转换为RocketMQ延时队列等级
     *
     * @param seconds 延时的秒数
     * @return 对应的延时等级，超过最大等级则返回最大等级
     */
    public int convertSecondsToDelayLevel(Long seconds) {
        return DELAY_LEVELS.stream()
                .filter(delay -> seconds <= delay)
                .findFirst()
                .map(delay -> DELAY_LEVELS.indexOf(delay) + 1)
                .orElse(DELAY_LEVELS.size());
    }

}
