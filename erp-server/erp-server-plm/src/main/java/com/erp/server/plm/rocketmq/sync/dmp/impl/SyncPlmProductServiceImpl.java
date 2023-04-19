package com.erp.server.plm.rocketmq.sync.dmp.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.plm.rocketmq.sync.dmp.SyncPlmProductService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 同步plm产品信息
 * @Author Luo_WG
 * @Date 2023/4/19 14:02
 **/
@Service
public class SyncPlmProductServiceImpl implements SyncPlmProductService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductDetailService productDetailService;

    /**
     * 同步产品信息表数据到中台表
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    @Override
    public void syncProductInfoToDmp() {
        List<ProductInfoEntity> list = productInfoService.lambdaQuery().list();
        // 异步推送到MQ
        list.stream().peek(msg ->{
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_INFO_TAG.getName(),msg, msg.getId());
        }).collect(Collectors.toList());;
    }

    /**
     * 产品sku表同步到中台
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    @Override
    public void syncProductSkuToDmp() {
        List<ProductDetailEntity> list = productDetailService.lambdaQuery().list();
        // 异步推送到MQ
        list.stream().peek(msg ->{
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_INFO_TAG.getName(),msg, msg.getId());
        }).collect(Collectors.toList());;
    }
}