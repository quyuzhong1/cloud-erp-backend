package com.erp.server.plm.rocketmq.sync.scm.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.plm.rocketmq.sync.scm.ScmSyncProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ScmSyncProductServiceImpl implements ScmSyncProductService {

    @Resource
    private MQProducerService mQProducerService;

    @Override
    public void syncProductInfoToScm(List<ProductInfoEntity> list) {
        List<List<ProductInfoEntity>> partitionList = ListUtil.partition(list, 200);
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_INFO_TAG.getName(),req, IdUtil.simpleUUID());
        });
    }

    @Override
    public void syncProductSkuToScm(List<ProductDetailEntity> list) {
        List<List<ProductDetailEntity>> partitionList = ListUtil.partition(list, 200);
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_SCM_PRODUCT_SKU_TAG.getName(),req, IdUtil.simpleUUID());
        });
    }
}
