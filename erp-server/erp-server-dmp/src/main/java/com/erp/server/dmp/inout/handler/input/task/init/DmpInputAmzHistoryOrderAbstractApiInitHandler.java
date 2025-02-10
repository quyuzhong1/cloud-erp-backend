package com.erp.server.dmp.inout.handler.input.task.init;

import com.common.business.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public abstract class DmpInputAmzHistoryOrderAbstractApiInitHandler extends DmpInputAmzCommonInitHandler {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 查询老中台mongo
     */
    public List<PlatformAmazonOrderDTO> findOrdersByCriteria(
            String startTime,
            String endTime,
            String platformShopCode,
            String orderStatus
            ) {
        Query query = new Query();
        // 添加 downloadTime 范围条件
        if (startTime != null && endTime != null) {
            query.addCriteria(Criteria.where("downloadTime").gte(startTime).lte(endTime));
        } else if (startTime != null) {
            query.addCriteria(Criteria.where("downloadTime").gte(startTime));
        } else if (endTime != null) {
            query.addCriteria(Criteria.where("downloadTime").lte(endTime));
        }

        // 添加 platformShopCode 条件
        if (platformShopCode != null) {
            query.addCriteria(Criteria.where("platformShopCode").is(platformShopCode));
        }

        // 添加 orderStatus 条件
        if (orderStatus != null) {
            query.addCriteria(Criteria.where("order.orderStatus").is(orderStatus));
        }
        query.addCriteria(Criteria.where("details").exists(true));
        query.addCriteria(Criteria.where("order.amazonOrderId").not().regex("S"));

        // 返回分页结果
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }


    public List<PlatformAmazonOrderDTO> findAllOrderMongoData(List<String> uniqueIds) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("uniqueId").in(uniqueIds)
        );
        return mongoTemplate.find(query, PlatformAmazonOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER);
    }


    public AmazonShopInfoDTO initShopInfoDTO(ShopInfoEntity shopInfo, List<ShopInfoEntity> relatedshopInfoList){
        Map<String, AmazonShopInfoDTO.ShopNameDTO> marketplaceShopIdMap = new HashMap<>();
        // 当前店铺站点
        marketplaceShopIdMap.put(
                AmazonMarketplaceEnum.getByCountryCode(shopInfo.getDictCountryCode()).getMarketplaceId(),
                new AmazonShopInfoDTO.ShopNameDTO(shopInfo.getId(), shopInfo.getName()));
        // 其他站点
        if (!CollectionUtils.isEmpty(relatedshopInfoList)){
            Map<String, AmazonShopInfoDTO.ShopNameDTO> relatedMap = relatedshopInfoList
                    .stream()
                    .collect(Collectors.toMap(e -> AmazonMarketplaceEnum.getByCountryCode(e.getDictCountryCode()).getMarketplaceId(),
                            e -> new AmazonShopInfoDTO.ShopNameDTO(e.getId(), e.getName())));
            marketplaceShopIdMap.putAll(relatedMap);
        }
        return new AmazonShopInfoDTO()
                // 店铺ID
                .setId(shopInfo.getId())
                // 访问token
                .setAccessToken("")
                // 刷新token
                .setRefreshToken("")
                // 店铺名称
                .setName(shopInfo.getName())
                // 区域id
                .setDictAreaCode(shopInfo.getDictAreaCode())
                // 国家id
                .setDictCountryCode(shopInfo.getDictCountryCode())
                // 负责人id
                .setChargeId(shopInfo.getChargeId())
                // 亚马逊账号ID
                .setPlatformShopCode(shopInfo.getPlatformShopCode())
                // 亚马逊账号授权的所有站点
                .setMarketplaceShopIdMap(marketplaceShopIdMap)
                ;
    }
}
