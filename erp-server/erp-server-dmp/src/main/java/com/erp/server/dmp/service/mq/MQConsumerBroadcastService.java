package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.server.dmp.service.DmpPullTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zdy
 * @ClassName MQConsumerBroadcastService
 * @description: rocket mq 广播消息监听
 * @date 2023年10月12日
 * @version: 1.0
 */
@Slf4j
@Component
public class MQConsumerBroadcastService {

    @Resource
    private DmpPullTaskService dmpPullTaskService;

    /**
     * 订单审核通过后同步dmp
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC,
            selectorExpression = "kingdee_so_info_tag",
            consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_INFO, messageModel = MessageModel.BROADCASTING)
    public class ConsumerApprovedOrderToDmp implements RocketMQListener<Map<String, Object>> {
        @Override
        public void onMessage(Map<String, Object> resultMap) {
            log.info("监听到订单审核通过同步任务回调：entity={}", JSONUtil.toJsonStr(resultMap));
            //处理订单同步
            dmpPullTaskService.syncOmsOrderToDmp(resultMap);
        }
    }

    /**
     * 换货订单审核通过后同步dmp
     */
    @Service
//    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_ORDER_TO_DMP_TOPIC,
//            selectorExpression = "approved_order_to_dmp_tag",
//            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-approved_order_to_dmp_consumer", messageModel = MessageModel.BROADCASTING)
    public class ConsumerApprovedDeliveryOrderToDmp implements RocketMQListener<Map<String, Object>> {
        @Override
        public void onMessage(Map<String, Object> resultMap) {
            log.info("监听到订单审核通过同步任务回调：entity={}", JSONUtil.toJsonStr(resultMap));
            //处理订单同步
            dmpPullTaskService.syncOmsOrderToDmp(resultMap);
        }
    }


    /**
     * 退货订单审核通过后同步dmp
     */
    @Service
//    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_ORDER_TO_DMP_TOPIC,
//            selectorExpression = "approved_order_to_dmp_tag",
//            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-approved_order_to_dmp_consumer", messageModel = MessageModel.BROADCASTING)
    public class ConsumerApprovedReturnOrderToDmp implements RocketMQListener<Map<String, Object>> {
        @Override
        public void onMessage(Map<String, Object> resultMap) {
            log.info("监听到订单审核通过同步任务回调：entity={}", JSONUtil.toJsonStr(resultMap));
            //处理订单同步
            dmpPullTaskService.syncOmsOrderToDmp(resultMap);
        }
    }
}
