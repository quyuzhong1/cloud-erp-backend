package com.erp.server.scm.mq.consumer;

import com.common.message.constant.RocketMqTopic;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.scm.service.ProductDetailService;
import com.erp.server.scm.service.ProductInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class ProductConsumer {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_dmp_product_info_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_info_consumer")
    public class ConsumerPlmProductInfo implements RocketMQListener<List<ProductInfoEntity>> {
        @Override
        public void onMessage(List<ProductInfoEntity> ext) {
            productInfoService.saveOrUpdateProductInfo(ext);
        }
    }

    @Service
    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC,
            selectorExpression = "sync_dmp_product_sku_tag",
            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-plm_product_detail_consumer")
    public class ConsumerPlmProductDetail implements RocketMQListener<List<ProductDetailEntity>> {
        @Override
        public void onMessage(List<ProductDetailEntity> ext) {
            productDetailService.saveOrUpdateProductDetail(ext);
        }
    }
}
