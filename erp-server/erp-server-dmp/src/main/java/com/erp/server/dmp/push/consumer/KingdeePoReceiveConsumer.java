package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.server.dmp.push.service.business.KingdeePoReceiveConsumerService;
import com.erp.server.dmp.push.service.business.KingdeeProductDetailConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 *
 * @Author Luo_WG
 * @Date 2023/10/11 9:00
 * @param
 * @return
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_po_receive_tag", consumerGroup = RocketMqConsumerGroup.SYNC_ERP_PO_RECEIVE,consumeMode = ConsumeMode.ORDERLY)
public class KingdeePoReceiveConsumer implements RocketMQListener<Map<String, Object>> {
    @Resource
    private KingdeePoReceiveConsumerService kingdeePoReceiveConsumerService;


    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeePoReceiveConsumerService.executeConsumer(map);
        }catch (Exception e){
            log.error("KingdeePoReceiveConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }

    }
}

