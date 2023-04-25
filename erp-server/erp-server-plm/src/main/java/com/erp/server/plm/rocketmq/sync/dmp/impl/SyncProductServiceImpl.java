package com.erp.server.plm.rocketmq.sync.dmp.impl;

import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.dto.NewProductDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.plm.rocketmq.sync.dmp.SyncProductService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProductSaleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 同步plm产品信息
 * @Author Luo_WG
 * @Date 2023/4/19 14:02
 **/
@Service
public class SyncProductServiceImpl implements SyncProductService {

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductSaleService productSaleService;

    /**
     * 同步产品信息表数据到中台表
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    @Override
    public void syncProductInfoToDmp() {
        List<ProductInfoEntity> list = productInfoService.getProductInfoAll();
        // 异步推送到MQ
        list.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_INFO_TAG.getName(),req, req.getId());
        });
    }

    /**
     * 产品sku表同步到中台
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    @Override
    public void syncProductSkuToDmp() {
        List<ProductDetailEntity> list = productDetailService.getProductDetailAll();
        // 异步推送到MQ
        list.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_SKU_TAG.getName(),req, req.getId());
        });
    }

    /**
     * 产品是否新品同步到中台
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    @Override
    public void syncNewProductToDmp() {
        List<NewProductDTO> list = productSaleService.getListingProductAll();
        // 异步推送到MQ
        list.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_LISTING_TAG.getName(),req, req.getId());
        });
    }
}