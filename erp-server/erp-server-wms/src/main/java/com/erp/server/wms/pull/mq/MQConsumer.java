package com.erp.server.wms.pull.mq;

import com.common.message.constant.RocketMqTopic;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.pull.service.ProductInfoService;
import com.erp.server.wms.pull.service.ProductSaleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @CreateTime: 2023-05-11  18:12
 * @Author: zhangchunlin
 */
@Slf4j
@Component
public class MQConsumer {

    @Autowired
    private ProductDetailService productDetailService;

    @Autowired
    private ProductSaleService productSaleService;

    @Autowired
    private ProductInfoService productInfoService;

    /**
     * 同步plm的产品信息
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_wms_product_sku_tag",
            consumerGroup = "${spring.profiles.active}-plm_product_detail_consumer")
    public class ConsumerPlmProductDetail implements RocketMQListener<ProductDetailEntity> {
        @Override
        public void onMessage(ProductDetailEntity entity) {
            try {
                productDetailService.saveOrUpdateProductDetail(entity);
            } catch (Exception e) {
                log.error("mq消息消费失败", e);
            }
        }
    }

    /**
     * 同步plm的产品销售信息
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_wms_product_sku_sale_tag",
            consumerGroup = "${spring.profiles.active}-plm_product_sale_consumer")
    public class ConsumerPlmProductSale implements RocketMQListener<ProductSaleEntity> {
        @Override
        public void onMessage(ProductSaleEntity entity) {
            try {
                productSaleService.saveOrUpdateProductSaleDetail(entity);
            }  catch (Exception e) {
                log.error("mq消息消费失败", e);
            }
        }
    }

    /**
     * 同步plm的产品详细信息
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_wms_product_info_tag",
            consumerGroup = "${spring.profiles.active}-plm_product_sale_consumer")
    public class ConsumerPlmProductInfoSale implements RocketMQListener<ProductInfoEntity> {
        @Override
        public void onMessage(ProductInfoEntity entity) {
            try {
                productInfoService.saveOrUpdateProductInfo(entity);
            }  catch (Exception e) {
                log.error("mq消息消费失败", e);
            }
        }
    }

}