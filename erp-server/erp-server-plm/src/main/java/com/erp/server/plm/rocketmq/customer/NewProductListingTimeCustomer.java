package com.erp.server.plm.rocketmq.customer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.RedisUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqNewConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqNewTopic;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.server.plm.service.ProductSaleService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqNewTopic.DMP_PRODUCT_LISTING_TO_PLM_TOPIC, selectorExpression = RocketMqNewTag.DMP_PRODUCT_LISTING_TO_PLM_TAG, consumerGroup = RocketMqNewConsumerGroup.DMP_PRODUCT_LISTING_TO_PLM_GROUP)
public class NewProductListingTimeCustomer implements RocketMQListener<String> {
    @Resource
    private ProductSaleService productSaleService;
    
    @Resource
    private RedisUtil redisUtil;

    @Override
    public void onMessage(String data) {
        try {
			log.info("新中台监听到上市时间需要修改：{}", data);
			JSONObject parseObject = JSON.parseObject(data);
			String sourcePlatform = parseObject.getString("sourcePlatform");
			String platformSkuNo = parseObject.getString("skuNo");
			String listingTime = parseObject.getString("listingTime");
			
			List<ListingInfoEntity> listingInfoEntityList = FeignQuery.create(ListingInfoEntity.class)
				.eq(ListingInfoEntity::getType, RuleTypeEnum.PLATFORM.getCode())
				.eq(ListingInfoEntity::getPlatformSkuNo, platformSkuNo)
				.last(" and LOWER(platform) = LOWER('"+ sourcePlatform +"') ")
				.list();
			if(CollUtil.isEmpty(listingInfoEntityList)) {
				return;
			}
			ListingInfoEntity listingInfoEntity = listingInfoEntityList.get(0);
			List<SkuMappingEntity> skuMappingEntityList = FeignQuery.create(SkuMappingEntity.class)
				.eq(SkuMappingEntity::getType, RuleTypeEnum.PLATFORM.getCode())
				.eq(SkuMappingEntity::getIsExpire, false)
				.eq(SkuMappingEntity::getDictPlatform, listingInfoEntity.getPlatform())
				.in(SkuMappingEntity::getListingId, listingInfoEntityList.stream().map(ListingInfoEntity::getId).collect(Collectors.toList()))
				.list();

			List<String> productIdList = skuMappingEntityList.stream().filter(s -> StringUtils.isNotBlank(s.getProductSkuId())).map(SkuMappingEntity::getProductSkuId).collect(Collectors.toList());
			if(CollUtil.isEmpty(productIdList)) {
				return;
			}
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			productSaleService.lambdaUpdate()
			        .set(ProductSaleEntity::getListingTime, LocalDate.parse(listingTime, dateTimeFormatter))
			        .in(ProductSaleEntity::getSkuId, productIdList)
			        .isNull(ProductSaleEntity::getListingTime)
			        .update();
			String key = RedisKeyConstant.PRODUCT_LISTING_TIME + sourcePlatform + ":" + platformSkuNo;
			redisUtil.set(key, listingTime);
		} catch (Throwable e) {
			log.error("新中台监听到上市时间需要修改失败：{}", data , e);
		}
    }
}
