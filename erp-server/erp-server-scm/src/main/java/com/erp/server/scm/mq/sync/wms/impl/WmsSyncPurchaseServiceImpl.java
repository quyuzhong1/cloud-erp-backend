package com.erp.server.scm.mq.sync.wms.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.server.scm.mq.sync.wms.WmsSyncPurchaseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class WmsSyncPurchaseServiceImpl implements WmsSyncPurchaseService {

    @Resource
    private MQProducerService mQProducerService;

    @Override
    public void syncPurchaseOrderToWms(List<PurchaseOrderEntity> list) {
        List<List<PurchaseOrderEntity>> partitionList = ListUtil.partition(list, 200);
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_TAG.getName(),req, IdUtil.simpleUUID());
        });
    }

    @Override
    public void syncPurchaseOrderDetailToWms(List<PurchaseOrderDetailEntity> list) {
        List<List<PurchaseOrderDetailEntity>> partitionList = ListUtil.partition(list, 200);
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG.getName(),req, IdUtil.simpleUUID());
        });
    }

    @Override
    public void syncPurchaseOrderSupplierToWms(List<PurchaseOrderSupplierEntity> list) {
        List<List<PurchaseOrderSupplierEntity>> partitionList = ListUtil.partition(list, 200);
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_SCM_TO_WMS_PURCHASE_TOPIC, RocketMqTagEnum.SYNC_WMS_PURCHASE_ORDER_SUPPLIER_TAG.getName(),req, IdUtil.simpleUUID());
        });
    }
}
