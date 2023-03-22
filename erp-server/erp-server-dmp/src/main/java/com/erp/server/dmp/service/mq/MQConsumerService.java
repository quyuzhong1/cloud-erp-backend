package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.pull.service.dmp.*;
import lombok.extern.slf4j.Slf4j;
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
     * rocketmq 监听销售订单相关数据
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_sales_order_tag||gyy_sales_history_order_tag||kingdee_sales_order_tag||mabang_sales_order_tag",
            consumerGroup = "${spring.profiles.active}-sales_order_consumer")
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
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_delivery_order_tag||kingdee_delivery_order_tag||mabang_delivery_order_tag",
            consumerGroup = "${spring.profiles.active}-sales_delivery_consumer")
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
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_refund_order_tag||kingdee_refund_order_tag||mabang_refund_order_tag",
            consumerGroup = "${spring.profiles.active}-sales_refund_order_consumer")
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
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_return_order_tag||kingdee_return_order_tag||mabang_return_order_tag",
            consumerGroup = "${spring.profiles.active}-sales_return_order_consumer")
    public class ConsumerErpReturnOrder implements RocketMQListener<DmpReturnOrderInfoEntity> {
        @Override
        public void onMessage(DmpReturnOrderInfoEntity ext) {
            log.info("监听退货订单消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpReturnOrderInfoService.checkOrder(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "gyy_shop_info_tag||kingdee_shop_info_tag||mabang_shop_info_tag||kingdee_ecc_shop_info_tag",
            consumerGroup = "${spring.profiles.active}-sales_shop_info_consumer")
    public class ConsumerErpShopInfo implements RocketMQListener<DmpShopInfoEntity> {
        @Override
        public void onMessage(DmpShopInfoEntity ext) {
            log.info("监听店铺信息消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            if(PlatformEnum.KINGDEE.getDesc().equals(ext.getPlatformSign()) || PlatformEnum.KINGDEE_ECC.getDesc().equals(ext.getPlatformSign())){
                dmpShopInfoService.checkShopByKingDee(ext);
            }else {
                dmpShopInfoService.checkOrder(ext);
            }

        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.DMP_ERP_ORDER_TOPIC,
            selectorExpression = "kingdee_sku_info_tag",
            consumerGroup = "${spring.profiles.active}-sales_sku_info_consumer")
    public class ConsumerErpSkuInfo implements RocketMQListener<DmpSkuInfoEntity> {
        @Override
        public void onMessage(DmpSkuInfoEntity ext) {
            log.info("监听商品信息消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpSkuInfoService.checkOrder(ext);
        }
    }


}
