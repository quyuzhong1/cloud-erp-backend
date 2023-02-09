package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.common.core.constant.RocketMqTopic;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.server.dmp.controller.CfgApiFieldMapController;
import com.erp.server.dmp.pull.service.dmp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Component
public class MQConsumerService {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpDeliveryDetailInfoService deliveryDetailInfoService;
    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;
    @Resource
    private DmpShopInfoService dmpShopInfoService;
    @Resource
    private DmpSkuInfoService dmpSkuInfoService;



    // topic需要和生产者的topic一致，consumerGroup属性是必须指定的，内容可以随意
    // selectorExpression的意思指的就是tag，默认为“*”，不设置的话会监听所有消息

    // 注意：这个ConsumerSend2和上面ConsumerSend在没有添加tag做区分时，不能共存，
    // 不然生产者发送一条消息，这两个都会去消费，如果类型不同会有一个报错，所以实际运用中最好加上tag，写这只是让你看知道就行

    /**
     * consumeMode = ConsumeMode.ORDERLY  每秒进行一次重试 一直重试
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC, selectorExpression = "tag2", consumerGroup = "Con_Group_Two", consumeMode = ConsumeMode.ORDERLY)
    public class ConsumerSend2 implements RocketMQListener<String> {
        @Override
        public void onMessage(String str) {
            log.info("监听到消息：str={}", str);
//            throw new RuntimeException("tag2 失败测试");
        }
    }

	// MessageExt：是一个消息接收通配符，不管发送的是String还是对象，都可接收，当然也可以像上面明确指定类型（我建议还是指定类型较方便）
    /**
     * consumeMode = ConsumeMode.CONCURRENTLY  重试16次
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC, selectorExpression = "tag1", consumerGroup = "Con_Group_Three", consumeMode = ConsumeMode.CONCURRENTLY)
    public class Consumer implements RocketMQListener<MessageExt> {
        @Override
        public void onMessage(MessageExt messageExt) {
            byte[] body = messageExt.getBody();
            String msg = new String(body);
            log.info("监听到消息：msg={}", msg);
            throw new RuntimeException("tag1 失败测试");
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC, selectorExpression = "tag3", consumerGroup = "test_group3")
    public class ConsumerSend3 implements RocketMQListener<CfgApiFieldMapController.TestMq> {
        @Override
        public void onMessage(CfgApiFieldMapController.TestMq entity) {
            log.info("监听到消息：str={}", entity);
        }
    }


    /**
     * rocketmq 监听销售订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC,
            selectorExpression = "gyy_sales_order_tag||gyy_sales_history_order_tag||kingdee_sales_order_tag||mabang_sales_order_tag",
            consumerGroup = "sales_order_consumer")
    public class ConsumerErpSalesOrder implements RocketMQListener<DmpOrderInfoEntity> {
        @Override
        public void onMessage(DmpOrderInfoEntity ext) {
            log.info("监听到销售订单消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpOrderInfoService.checkOrder(ext);
        }
    }

    /**
     * rocketmq 监听发货订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC,
            selectorExpression = "gyy_delivery_order_tag||kingdee_delivery_order_tag||mabang_delivery_order_tag",
            consumerGroup = "sales_delivery_consumer")
    public class ConsumerErpDeliveryOrder implements RocketMQListener<DmpDeliveryDetailInfoEntity> {
        @Override
        public void onMessage(DmpDeliveryDetailInfoEntity ext) {
            log.info("监听到发货订单消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            deliveryDetailInfoService.checkOrder(ext);
        }
    }

    /**
     * rocketmq 监听退款订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC,
            selectorExpression = "gyy_refund_order_tag||kingdee_refund_order_tag||mabang_refund_order_tag",
            consumerGroup = "sales_refund_order_consumer")
    public class ConsumerErpRefundOrder implements RocketMQListener<DmpRefundInfoEntity> {
        @Override
        public void onMessage(DmpRefundInfoEntity ext) {
            log.info("监听退款订单消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpRefundInfoService.checkOrder(ext);
        }
    }

    /**
     * rocketmq 监听退款订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC,
            selectorExpression = "gyy_return_order_tag||kingdee_return_order_tag||mabang_return_order_tag",
            consumerGroup = "sales_return_order_consumer")
    public class ConsumerErpReturnOrder implements RocketMQListener<DmpReturnOrderInfoEntity> {
        @Override
        public void onMessage(DmpReturnOrderInfoEntity ext) {
            log.info("监听退货订单消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpReturnOrderInfoService.checkOrder(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC,
            selectorExpression = "gyy_shop_info_tag||kingdee_shop_info_tag||mabang_shop_info_tag",
            consumerGroup = "sales_shop_info_consumer")
    public class ConsumerErpShopInfo implements RocketMQListener<DmpShopInfoEntity> {
        @Override
        public void onMessage(DmpShopInfoEntity ext) {
            log.info("监听店铺信息消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            if(ext.getPlatformSign().equals(PlatformEnum.KINGDEE.getDesc())){
                dmpShopInfoService.checkShopByKingDee(ext);
            }else {
                dmpShopInfoService.checkOrder(ext);
            }

        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_TOPIC,
            selectorExpression = "gyy_sku_info_tag||kingdee_sku_info_tag||mabang_sku_info_tag",
            consumerGroup = "sales_sku_info_consumer")
    public class ConsumerErpSkuInfo implements RocketMQListener<DmpSkuInfoEntity> {
        @Override
        public void onMessage(DmpSkuInfoEntity ext) {
            log.info("监听商品信息消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpSkuInfoService.checkOrder(ext);
        }
    }


}
