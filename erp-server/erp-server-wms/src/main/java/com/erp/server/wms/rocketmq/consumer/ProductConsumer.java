package com.erp.server.wms.rocketmq.consumer;

import com.common.message.constant.RocketMqTopic;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.server.wms.service.PurchaseOrderDetailService;
import com.erp.server.wms.service.PurchaseOrderService;
import com.erp.server.wms.service.PurchaseOrderSupplierService;
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
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

//    @Service
//    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC,
//            selectorExpression = "sync_wms_purchase_order_tag",
//            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-wms_purchase_order_consumer")
//    public class ConsumerWmsPurchaseOrder implements RocketMQListener<List<PurchaseOrderEntity>> {
//        @Override
//        public void onMessage(List<PurchaseOrderEntity> ext) {
//            purchaseOrderService.saveOrUpdatePurchaseOrder(ext);
//        }
//    }
//
//    @Service
//    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC,
//            selectorExpression = "sync_wms_purchase_order_detail_tag",
//            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-wms_purchase_order_detail_consumer")
//    public class ConsumerWmsPurchaseDetail implements RocketMQListener<List<PurchaseOrderDetailEntity>> {
//        @Override
//        public void onMessage(List<PurchaseOrderDetailEntity> ext) {
//            purchaseOrderDetailService.saveOrUpdatePurchaseOrderDetail(ext);
//        }
//    }
//
//    @Service
//    @RocketMQMessageListener(topic = RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC,
//            selectorExpression = "sync_wms_purchase_order_supplier_tag",
//            consumerGroup = "${spring.cloud.nacos.discovery.namespace}-wms_purchase_order_supplier_consumer")
//    public class ConsumerWmsPurchaseOrderSupplier implements RocketMQListener<List<PurchaseOrderSupplierEntity>> {
//        @Override
//        public void onMessage(List<PurchaseOrderSupplierEntity> ext) {
//            purchaseOrderSupplierService.saveOrUpdatePurchaseOrderSupplier(ext);
//        }
//    }
}

