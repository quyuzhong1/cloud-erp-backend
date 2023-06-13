package com.erp.server.plm.rocketmq.sync.dmp.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.ObjectUtils;
import com.common.message.constant.RedisKeyConstant;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private ProductSaleService productSaleService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 同步产品信息表数据到中台表
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    @Override
    public void syncProductInfoToDmp(List<ProductInfoEntity> list) {
        List<List<ProductInfoEntity>> partitionList = ListUtil.partition(list, 200);
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_INFO_TAG.getName(),req, IdUtil.simpleUUID());
        });
    }

    /**
     * 产品sku表同步到中台
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    @Override
    public void syncProductSkuToDmp(List<ProductDetailEntity> list) {
        List<List<ProductDetailEntity>> partitionList = ListUtil.partition(list, 200);
        // 异步推送到MQ
        partitionList.forEach(req -> {
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_SKU_TAG.getName(),req, IdUtil.simpleUUID());
        });
    }

    /**
     * 产品是否新品同步到中台
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    @Override
    public void syncNewProductToDmp() {
        List<NewProductDTO> listingNotNullList = productSaleService.getListingProductAll(Boolean.TRUE);
        List<NewProductDTO> listingNullList = productSaleService.getListingProductAll(Boolean.FALSE);

        Map<String,List<NewProductDTO>> map = new HashMap<>();
        map.put("listingNotNullList", listingNotNullList);
        listingNotNullList.forEach(req -> {
            if (ObjectUtil.isNotEmpty(redisUtil.hget(RedisKeyConstant.SKU_LISTING_TIME, req.getSkuNo()))) {
                redisUtil.hset(RedisKeyConstant.SKU_LISTING_TIME, req.getSkuNo(), req.getNewListingTime(), 30 * 24 * 3600);
            }
        });
        // 异步推送到MQ
        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.SYNC_DMP_PRODUCT_LISTING_TAG.getName(), map, UUID.randomUUID().toString());

        Map<String,List<NewProductDTO>> listingNullMap = new HashMap<>();
        listingNullMap.put("listingNullList", listingNullList);
        // 异步推送到MQ
        mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.GET_DMP_PRODUCT_LISTING_TAG.getName(), listingNullMap, UUID.randomUUID().toString());
    }
}