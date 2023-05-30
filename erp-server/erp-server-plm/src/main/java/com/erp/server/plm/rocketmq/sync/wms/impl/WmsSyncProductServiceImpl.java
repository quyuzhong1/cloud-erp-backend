package com.erp.server.plm.rocketmq.sync.wms.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.plm.rocketmq.sync.wms.WmsSyncProductService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProductSaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 同步plm产品信息
 * @Author Luo_WG
 * @Date 2023/4/19 14:02
 **/
@Slf4j
@Service
public class WmsSyncProductServiceImpl implements WmsSyncProductService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductSaleService productSaleService;


    @Override
    public void syncProductSkuToWms() {
        log.info("开始同步产品sku到wms系统");
        List<ProductDetailEntity> list = productDetailService.getProductDetailAll();
        List<List<ProductDetailEntity>> partitionList = ListUtil.partition(list, 100);// 按100个拆分
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_SKU_TAG.getName(),req,  IdUtil.simpleUUID());
        });
        log.info("结束同步产品sku到wms系统");
    }

    @Override
    public void syncProductSkuSaleToWms() {
        log.info("开始同步产品sku销售信息到wms系统");
        List<ProductSaleEntity> list = productSaleService.list();
        List<List<ProductSaleEntity>> partitionList = ListUtil.partition(list, 100);// 按100个拆分
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_SKU_SALE_TAG.getName(),req, IdUtil.simpleUUID());
        });
        log.info("结束同步产品sku销售信息到wms系统");
    }

    @Override
    public void syncProductInfoToWms() {
        log.info("开始同步产品详细信息到wms系统");
        List<ProductInfoEntity> list = productInfoService.getProductInfoAll();
        List<List<ProductInfoEntity>> partitionList = ListUtil.partition(list, 100);// 按100个拆分
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_TO_WMS_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_WMS_PRODUCT_INFO_TAG.getName(),req,  IdUtil.simpleUUID());
        });
        log.info("结束同步产品详细信息到wms系统");
    }
}