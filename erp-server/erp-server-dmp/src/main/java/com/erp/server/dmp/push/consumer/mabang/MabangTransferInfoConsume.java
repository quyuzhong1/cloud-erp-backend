package com.erp.server.dmp.push.consumer.mabang;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 直接调拨单推送到马帮
 * @CreateTime: 2023-06-27  14:54
 * @Author: zhangchunlin
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_ERP_TO_MABANG_TOPIC, selectorExpression = "mabang_transfer_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_MABANG_TRANSFER_INFO)
public class MabangTransferInfoConsume implements RocketMQListener<Map<String, Object>> {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Map<String, Object> map) {

    }


}