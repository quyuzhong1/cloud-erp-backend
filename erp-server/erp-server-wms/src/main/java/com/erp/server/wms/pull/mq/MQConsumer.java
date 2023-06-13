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

import java.util.List;

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
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC,
            selectorExpression = "sync_wms_product_sku_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_detail_consumer")
    public class ConsumerPlmProductDetail implements RocketMQListener<List<ProductDetailEntity>> {
        @Override
        public void onMessage(List<ProductDetailEntity> entities) {
            try {
                log.info("监听到plm产品sku信息");
                productDetailService.saveOrUpdateProductDetail(entities);
            } catch (Exception e) {
                log.error("mq消息消费失败", e);
            }
        }
    }

    /**
     * 同步plm的产品销售信息
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC,
            selectorExpression = "sync_wms_product_sku_sale_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_sale_consumer")
    public class ConsumerPlmProductSale implements RocketMQListener<List<ProductSaleEntity>> {
        @Override
        public void onMessage(List<ProductSaleEntity> entities) {
            try {
                log.info("监听到plm产品sku销售信息");
                productSaleService.saveOrUpdateProductSaleDetail(entities);
            }  catch (Exception e) {
                log.error("mq消息消费失败", e);
            }
        }
    }

    /**
     * 同步plm的产品详细信息
     */
    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC,
            selectorExpression = "sync_wms_product_info_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_info_consumer")
    public class ConsumerPlmProductInfoSale implements RocketMQListener<List<ProductInfoEntity>> {
        @Override
        public void onMessage(List<ProductInfoEntity> entities) {
            try {
                log.info("监听到plm产品信息");
                productInfoService.saveOrUpdateProductInfo(entities);
            }  catch (Exception e) {
                log.error("mq消息消费失败", e);
            }
        }
    }

}