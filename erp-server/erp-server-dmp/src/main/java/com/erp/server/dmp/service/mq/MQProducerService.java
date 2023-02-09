package com.erp.server.dmp.service.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.utils.IdUtils;
import com.erp.common.entity.MessageBody;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	private void sendMsg(MSG_TYPE msgType,String msgKey, String destination, Object payload, String msgSource){
        if(StrUtil.isBlank(msgKey)){
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
                        throw new RuntimeException(String.format("消息发送失败 topic_tag:%s", destination ));
                    }
                });
                break;
            case SYNC:
                result = rocketMQTemplate.syncSend(destination, message);
                break;
        }
        log.info("消息发送 MQService 结束: result: {} dest: {} msg: {}}", JSONUtil.toJsonStr(result), destination, message);
    }

    /**
     * 同步发送消息,会确认应答
     * @param destination
     * @param payload
     */
    public void syncSendMsg(String msgKey, String destination, Object payload, String msgSource){
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
        String destination = topic + ":" + tag;
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
    public void oneWaySendMsg(String msgKey, String destination, Object payload, String msgSource){
        sendMsg(MSG_TYPE.ONEWAY, msgKey,destination, payload,msgSource);
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
        String destination = topic + ":" + tag;
        oneWaySendMsg(msgKey, destination, payload,msgSource);
    }


    public SendResult batchSendEntity(String msgKey,String topic, String tag, List<T> entityList) {
        List<Message<T>> msgList = entityList.stream()
                .map(x ->
                MessageBuilder.withPayload(x).build())
                .collect(Collectors.toList());
        SendResult result = rocketMQTemplate.syncSend(topic+tag, msgList, 60000);
        return result;
    }

    /**
     * 普通发送（这里的参数对象User可以随意定义，可以发送个对象，也可以是字符串等）
     */
    public void sendEntity(String msgKey, String topic, String tag, T entity) {
        rocketMQTemplate.convertAndSend(topic + tag, entity);
    }

    /**
     * 普通发送（这里的参数对象User可以随意定义，可以发送个对象，也可以是字符串等）
     */
    public SendResult sendMsg(String msgKey, String topic, String tag, Message msg) {
         return rocketMQTemplate.syncSend(topic + tag, msg);
    }

    public SendResult sendBachMsg(String topic, String tag, List msgs) {
        List<Message<String>> messageList = new ArrayList<>();
        for (Object msg : msgs) {
            messageList.add(MessageBuilder.withPayload("批量消息" + msg).build());
        }
        return rocketMQTemplate.syncSend(topic + tag, messageList);
    }

    public SendResult syncClassMsg(String topic, String tag, T entity, String key) {
        Message<T> msg = MessageBuilder.withPayload(entity)
                .setHeader(RocketMQHeaders.KEYS, key)
                .build();
        return rocketMQTemplate.syncSend(StrUtil.format("{}:{}", topic, tag), msg);
    }

    public void asyncClassMsg(String topic, String tag, T entity, String key) {
        Message<T> msg = MessageBuilder.withPayload(entity)
                .setHeader(RocketMQHeaders.KEYS, key)
                .build();
        String destination = StrUtil.format("{}:{}", topic, tag);
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
}
