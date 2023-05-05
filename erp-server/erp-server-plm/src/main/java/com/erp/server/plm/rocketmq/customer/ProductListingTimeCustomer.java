package com.erp.server.plm.rocketmq.customer;

import cn.hutool.json.JSONUtil;
import com.common.business.utils.RedisUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductSaleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, selectorExpression = "product_listing_update_tag", consumerGroup = RocketMqConsumerGroup.PRODUCT_LISTING_UPDATE)
public class ProductListingTimeCustomer implements RocketMQListener<Map<String, Object>> {
    @Resource
    private ProductSaleService productSaleService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void onMessage(Map<String, Object> stringObjectMap) {
        log.info("监听到上市时间需要修改：entity={}", JSONUtil.toJsonStr(stringObjectMap));
        String skuNo = String.valueOf(stringObjectMap.get("skuNo"));
        String listingTime = String.valueOf(stringObjectMap.get("listingTime"));
        ProductDetailEntity productIdBySku = productDetailService.getProductIdBySku(skuNo);

        ProductSaleEntity bySkuId = productSaleService.getBySkuId(productIdBySku.getId());
        if (bySkuId.getListingTime() != null) {
            listingTime = bySkuId.getListingTime().toString();
        }
        productSaleService.lambdaUpdate()
                .set(ProductSaleEntity::getListingTime, listingTime)
                .eq(ProductSaleEntity::getSkuId, productIdBySku.getId())
                .update();
        redisUtil.hset(RedisKeyConstant.SKU_LISTING_TIME, skuNo, listingTime, 30 * 24 * 3600);
    }
}
