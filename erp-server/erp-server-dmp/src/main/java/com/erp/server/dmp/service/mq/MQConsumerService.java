package com.erp.server.dmp.service.mq;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.dto.NewProductDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.dmp.pull.service.dmp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private DmpOrderItemService dmpOrderItemService;

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
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_order_consumer")
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
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_delivery_consumer")
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
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_refund_order_consumer")
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
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_return_order_consumer")
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
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_shop_info_consumer")
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
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-sales_sku_info_consumer")
    public class ConsumerErpSkuInfo implements RocketMQListener<DmpSkuInfoEntity> {
        @Override
        public void onMessage(DmpSkuInfoEntity ext) {
            log.info("监听商品信息消息：entity={}", JSONUtil.toJsonStr(ext));
            // 调用订单写入与更新
            dmpSkuInfoService.checkOrder(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_dmp_product_info_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_info_consumer")
    public class ConsumerPlmProductInfo implements RocketMQListener<ProductInfoEntity> {
        @Override
        public void onMessage(ProductInfoEntity ext) {
            productInfoService.saveOrUpdateProductInfo(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_dmp_product_sku_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_detail_consumer")
    public class ConsumerPlmProductDetail implements RocketMQListener<ProductDetailEntity> {
        @Override
        public void onMessage(ProductDetailEntity ext) {
            productDetailService.saveOrUpdateProductDetail(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_dmp_product_listing_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_listing_consumer")
    public class ConsumerPlmProductListing implements RocketMQListener<Map<String, List<NewProductDTO>>>  {
        @Override
        public void onMessage(Map<String,List<NewProductDTO>> ext) {
            dmpOrderItemService.updateNewSign(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "get_dmp_product_listing_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-get_product_listing_consumer")
    public class ConsumerGetProductListing implements RocketMQListener<Map<String, List<NewProductDTO>>>  {
        @Override
        public void onMessage(Map<String,List<NewProductDTO>> ext) {
            dmpOrderItemService.getProductListing(ext);
        }
    }
}
