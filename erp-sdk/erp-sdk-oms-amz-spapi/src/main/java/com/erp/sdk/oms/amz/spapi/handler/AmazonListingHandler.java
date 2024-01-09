package com.erp.sdk.oms.amz.spapi.handler;

import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractProductHandler;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.api.CatalogApi;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.convert.SdkListingConverter;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonListingDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportListingMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonIncludedDataEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.Item;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiRateLimitUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 亚马逊产品处理器
 *
 * @Author Cloud
 * @Date 2023/8/31 15:48
 **/
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.AMAZON)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class AmazonListingHandler extends AbstractProductHandler<PlatformAmazonListingDTO, PlatformProductDTO> {

    @Resource
    private DmpAmazonFeign dmpAmazonFeign;

    @Resource
    private AmazonSpApiRateLimitUtils amazonSpApiRateLimitUtils;

    @Resource
    private RedisUtil redisUtil;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlatformAmazonListingDTO> download(JobTaskDTO data) {
        // 亚马逊商品(从已下载的mongo获取)
        List<?> genericMongoDataList = data.getMongoDataList();
        if (CollectionUtils.isEmpty(genericMongoDataList)) {
            return Collections.emptyList();
        }

        Object mongoData = data.getMongoDataList().stream().findFirst().orElse(null);
        if (!(mongoData instanceof ReportListingMongoDTO)) {
            throw new ServiceException("mongoDataList类型异常:error=" + genericMongoDataList.getClass().toGenericString());
        }
        List<ReportListingMongoDTO> mongoDataList = (List<ReportListingMongoDTO>) genericMongoDataList;
        // 过滤异常数据
        mongoDataList = mongoDataList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getSellerSku()) && StringUtils.isNotBlank(e.getAsin1()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(mongoDataList)){
            return Collections.emptyList();
        }

        // 返回下载源数据
        return mongoDataList.stream()
                .map(e -> SdkListingConverter.INSTANCE.mongoDtoToListingDto(e, data.getShopId()))
                .collect(Collectors.toList());

    }


    @Override
    public List<PlatformProductDTO> convert(List<PlatformAmazonListingDTO> sourceDataList) {
        // 亚马逊商品转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(PlatformAmazonListingDTO::convertDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }

    @Override
    public PlatformAmazonListingDTO downloadDetail(PlatformAmazonListingDTO dto, JSONObject extendObj) {
        String shopId = dto.getShopId();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException("未找到对应shopId， uniqueId=" + dto.getUniqueId() + "shopId="+ shopId);
        }
        String asin = dto.getAsin1();
        if (StringUtils.isBlank(asin)) {
            throw new ServiceException("未找到对应asin， uniqueId=" + dto.getUniqueId() + "shopId="+ shopId + ", asin={}" + asin);
        }
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }
        // 默认请求速率配置
        String limitKey = extendObj.getString(AmazonRequestTypeRateLimiterEnum.limitKey);
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.LISTING_ITEMS;
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);


        // 产品规格信息
        String productSpec;
        // 包装信息
        String packing;
        // 图片
        String imageUrl;
        try {
             //查询商品详情
            CatalogApi catalogApi =  CatalogApi.init(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false,  rateLimitConfig);
            List<String> marketplaceIds = Collections.singletonList(marketPlaceEnum.getMarketplaceId());
            List<String> includedData = AmazonIncludedDataEnum.getAllWithoutVendor();
            ApiResponse<Item> itemResponse = catalogApi.getCatalogItemWithHttpInfo(asin, marketplaceIds, includedData, null);
            List<String> limitArray = itemResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            String rateLimitStr = limitArray.get(0);
            if (StringUtils.isNotBlank(rateLimitStr)){
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN);
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            Item response = itemResponse.getData();

            // 拼接产品规格信息
            productSpec = response.combineProductSpec(marketPlaceEnum.getMarketplaceId());
            // 拼接包装信息
            packing = response.combinePacking(marketPlaceEnum.getMarketplaceId());
            // 找出第一张图片信息
            imageUrl = response.combineImage(marketPlaceEnum.getMarketplaceId());
            // 源信息
            dto.setDetail(response);
        } catch (ApiException e) {
            throw new ServiceException("[Amazon SP-APi] 下载listing失败" + e);
        }

        // 产品规格信息
        dto.setProductSpec(productSpec);
        // 产品包装信息
        dto.setProductPacking(packing);
        // 产品图片
        dto.setImageUrl(imageUrl);

        return dto;
    }

}
