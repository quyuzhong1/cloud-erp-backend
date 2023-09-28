package com.erp.server.dmp.push.consumer;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.server.dmp.push.service.business.KingdeeWarehouseConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 对接金蝶仓库
 *
 * @author Lambda
 * @Classname KingdeeWarehouseConsumer
 * @Date 2023-04-25 14:29
 * @Created by yl
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_warehouse_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_WAREHOUSE,consumeMode = ConsumeMode.ORDERLY)
public class KingdeeWarehouseConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeWarehouseConsumerService kingdeeWarehouseConsumerService;

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeWarehouseConsumerService.executeConsumer(map);
        }catch (Exception e){
            log.error("KingdeeWarehouseConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }


    }


}
