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

        dmpPushMsgService.save(dmpPushMsgEntity);

        // 发送微信订阅消息
        wxMiniAppService.sendSubscribeMsg(dmpPushMsgEntity.getPushData());
    }


//    @Resource
//    private ShopInfoMappingService shopInfoMappingService;
//    @Resource
//    private WmsShipmentFeign wmsShipmentFeign;
//    @Resource
//    private MongoService mongoService;
//
//    /**
//     * rocketmq 监听发货订单相关数据
//     */
//    @Service
//    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
//            selectorExpression = "lx_fba_shipment_receive_tag",
//            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-lx_fba_receive_consumer")
//    public class ConsumerErpFbaReceive implements RocketMQListener {
//        @Override
//        public void onMessage(Object extObj) {
//            log.info("监听领星Fba签收明细消息：entity={}", JSONUtil.toJsonStr(extObj));
//            try {
//                FbaReceiveGroupEntity ext = JSONUtil.toBean(extObj.toString(), FbaReceiveGroupEntity.class);
//                // 检查店铺ID
//                if (null == ext.getShopId()){
//                    throw new ServiceException(StrUtil.format("来源数据异常, 店铺ID为空, dto={}", extObj.toString()));
//                }
//                // 保存和检查调拨
//                wmsShipmentFeign.saveAndCheckTransfer(ext);
//                MapUtil mapUtil = getMapParam();
//                UniqueDto updateDto = UniqueDto.getUniqId(ext.getUniqueId());
//                finishClean(mapUtil, updateDto,MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE, FbaReceiveGroupEntity.class);
//            } catch (Throwable e) {
//                log.error("监听领星Fba签收明细消费失败：error={}", ExceptionUtil.stacktraceToString(e));
//            }
//        }
//    }

}
