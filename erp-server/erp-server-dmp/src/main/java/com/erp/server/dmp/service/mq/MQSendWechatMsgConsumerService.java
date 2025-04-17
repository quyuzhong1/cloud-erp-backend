package com.erp.server.dmp.service.mq;

import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.server.dmp.service.DmpPushMsgService;
import com.sdk.wx.miniapp.api.WxMiniAppService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * DMP 微信订阅消息 消费者
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.DMP_WECHAT_SUBSCRIBE_MSG_TOPIC,
        selectorExpression = "dmp_wechat_subscribe_msg_tag",
        consumerGroup = RocketMqConsumerGroup.DMP_WECHAT_SUBSCRIBE_MSG_CONSUMER)
public class MQSendWechatMsgConsumerService implements RocketMQListener<DmpPushMsgEntity> {

    @Resource
    private WxMiniAppService wxMiniAppService;

    @Resource
    private DmpPushMsgService dmpPushMsgService;

    @Override
    public void onMessage(DmpPushMsgEntity dmpPushMsgEntity) {
        // 发送微信订阅消息
        String result = wxMiniAppService.sendSubscribeMsg(dmpPushMsgEntity.getPushData());
        log.info("【{}】发送微信订阅消息结果：{}",dmpPushMsgEntity.getSourceCode(),result);
        dmpPushMsgEntity.setRemark(result);
        dmpPushMsgService.save(dmpPushMsgEntity);
    }
}
