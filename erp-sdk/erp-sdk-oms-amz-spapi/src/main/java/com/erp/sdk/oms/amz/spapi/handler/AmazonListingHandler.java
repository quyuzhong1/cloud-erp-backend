package com.erp.sdk.oms.amz.spapi.handler;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
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
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.RateLimitConfiguration;
import com.erp.sdk.oms.amz.spapi.api.CatalogApi;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.convert.SdkListingConverter;
import com.erp.sdk.oms.amz.spapi.csv.ReportListingCsvEntity;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonListingDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportListingMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonIdentifiersTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonIncludedDataEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.Item;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.ItemSearchResults;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiRateLimitUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
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
        // 亚马逊商品(从已下载的获取)
        List<?> genericDataList = data.getSourceList();
        if (CollectionUtils.isEmpty(genericDataList)) {
            return Collections.emptyList();
        }

        Object sourceData = data.getSourceList().stream().findFirst().orElse(null);
        if (!(sourceData instanceof ReportListingMongoDTO)) {
            throw new ServiceException("sourceData类型异常:error=" + genericDataList.getClass().toGenericString());
        }
        List<ReportListingMongoDTO> sourceDataList = (List<ReportListingMongoDTO>) genericDataList;
        // 过滤异常数据
        sourceDataList = sourceDataList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getSellerSku()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sourceDataList)) {
            return Collections.emptyList();
        }

        // 返回下载源数据
        return sourceDataList.stream()
                .map(e -> SdkListingConverter.INSTANCE.sourceDtoToListingDto(e,
                        e.getRequestShopId(),
                        LocalDateTimeUtil.parse(e.getReportDataEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                )
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
            throw new ServiceException("未找到对应shopId， uniqueId=" + dto.getUniqueId() + "shopId=" + shopId);
        }
        String asin = dto.getAsin1();
        if (StringUtils.isBlank(asin)) {
            throw new ServiceException("未找到对应asin， uniqueId=" + dto.getUniqueId() + "shopId=" + shopId + ", asin={}" + asin);
        }
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }
        // 默认请求速率配置
        String limitKey = extendObj.getString(AmazonRequestTypeRateLimiterEnum.limitKey);
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.PRODUCT_ITEMS;
        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);

        // 产品规格信息
        String productSpec;
        // 包装信息
        String packing;
        // 图片
        String imageUrl;
        try {
            //查询商品详情
            CatalogApi catalogApi = CatalogApi.init(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);
            List<String> marketplaceIds = Collections.singletonList(marketPlaceEnum.getMarketplaceId());
            List<String> includedData = AmazonIncludedDataEnum.getAllWithoutVendor();
            ApiResponse<Item> itemResponse = catalogApi.getCatalogItemWithHttpInfo(asin, marketplaceIds, includedData, null);
            List<String> limitArray = itemResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            String rateLimitStr = limitArray.get(0);
            if (StringUtils.isNotBlank(rateLimitStr)) {
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

    /**
     * 根据IdentifiersType分组查询
     */
    public List<PlatformAmazonListingDTO> downloadDetailListByIdentifiersType(List<PlatformAmazonListingDTO> currentListingDTOList, JSONObject extendObj, AmazonShopInfoDTO shopInfoDTO, AmazonMarketplaceEnum marketPlaceEnum, Integer size) {
        // 默认请求速率配置
        String limitKey = extendObj.getString(AmazonRequestTypeRateLimiterEnum.limitKey);
//        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.PRODUCT_ITEMS;
//        RateLimitConfiguration rateLimitConfig = amazonSpApiRateLimitUtils.buildConfig(requestTypeRateLimiterEnum, limitKey);
//        RateLimitConfiguration rateLimitConfig = null;

        Map<AmazonIdentifiersTypeEnum, List<PlatformAmazonListingDTO>> listMap = currentListingDTOList.stream().collect(Collectors.groupingBy(e -> e.convertIdentifiersType(marketPlaceEnum)));

        //查询商品详情
//      CatalogApi catalogApi = CatalogApi.init(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, rateLimitConfig);
        CatalogApi catalogApi = CatalogApi.init(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

        List<PlatformAmazonListingDTO> resultList = new LinkedList<>();
        for (Map.Entry<AmazonIdentifiersTypeEnum, List<PlatformAmazonListingDTO>> entry : listMap.entrySet()) {
            List<String> marketplaceIds = Collections.singletonList(marketPlaceEnum.getMarketplaceId());
            List<String> identifiers = entry.getValue()
                    .stream()
                    .map(PlatformAmazonListingDTO::checkAndGetIdentifier).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            String identifiersType = entry.getKey().getCode();
            List<String> includedData = AmazonIncludedDataEnum.getAllWithoutVendor();
            String locale = null;
            String sellerId = null;
            List<String> keywords = null;
            List<String> brandNames = null;
            List<String> classificationIds = null;
            Integer pageSize = size;
            String pageToken = null;
            String keywordsLocale = null;
            ApiResponse<ItemSearchResults> apiResponse;
            try {
                apiResponse = catalogApi.searchCatalogItemsWithHttpInfo(marketplaceIds, identifiers, identifiersType, includedData, locale, sellerId, keywords, brandNames, classificationIds, pageSize, pageToken, keywordsLocale);
            } catch (ApiException e) {
                throw new ServiceException("[Amazon SP-APi] 下载listing失败" + e);
            }
            amazonSpApiRateLimitUtils.checkAndSetRedis(limitKey, apiResponse);
            List<Item> items = apiResponse.getData().getItems();
            if (CollectionUtils.isEmpty(items)) {
                return Collections.emptyList();
            }
            // 根据identifiers.identifierType区分返回的来源的ProductId
            Map<String, Item> itemMap = items.stream().collect(Collectors.toMap(e -> e.getKeyByIdentifierType(entry.getKey(), marketPlaceEnum), Function.identity()));
            entry.getValue().forEach(e -> {
                Item item = itemMap.get(e.getProductId());
                if (null != item) {
                    this.setAllDetail(e, item, marketPlaceEnum);
                }
            });
            resultList.addAll(entry.getValue());
        }

        return resultList;
    }

    private void setAllDetail(PlatformAmazonListingDTO dto, Item item, AmazonMarketplaceEnum marketPlaceEnum) {
        // 拼接产品规格信息
        String productSpec = item.combineProductSpec(marketPlaceEnum.getMarketplaceId());
        // 拼接包装信息
        String packing = item.combinePacking(marketPlaceEnum.getMarketplaceId());
        // 找出第一张图片信息
        String imageUrl = item.combineImage(marketPlaceEnum.getMarketplaceId());
        // 源信息
        dto.setDetail(item);
        // 产品规格信息
        dto.setProductSpec(productSpec);
        // 产品包装信息
        dto.setProductPacking(packing);
        // 产品图片
        dto.setImageUrl(imageUrl);
        // ASIN
        dto.setAsin1(item.getAsin());
    }


    /**
     * 新根据IdentifiersType分组查询
     */
    public List<JSONObject> newDownloadDetailListByIdentifiersType(List<ReportListingMongoDTO> currentListingDTOList, JSONObject extendObj, AmazonShopInfoDTO shopInfoDTO, AmazonMarketplaceEnum marketPlaceEnum, Integer size) {
        // 默认请求速率配置
        String limitKey = extendObj.getString(AmazonRequestTypeRateLimiterEnum.limitKey);
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            String msg = StrUtil.format("【亚马逊明细拉取】 taskId={}, groupId={},存在429等待恢复:放弃当前请求任务", extendObj.getString("taskId"), limitKey);
            ServiceException.runError(msg);
        }
        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.PRODUCT_ITEMS.getRateLimit();

        Map<AmazonIdentifiersTypeEnum, List<ReportListingMongoDTO>> listMap = currentListingDTOList.stream().collect(Collectors.groupingBy(ReportListingMongoDTO::convertIdentifiersType));

        //查询商品详情
        CatalogApi catalogApi = CatalogApi.init(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

        List<String> marketplaceIds = Collections.singletonList(marketPlaceEnum.getMarketplaceId());

        List<JSONObject> resultList = new LinkedList<>();
        for (Map.Entry<AmazonIdentifiersTypeEnum, List<ReportListingMongoDTO>> entry : listMap.entrySet()) {
            List<String> identifiers = entry.getValue()
                    .stream()
                    .map(ReportListingMongoDTO::checkAndGetIdentifier).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            String identifiersType = entry.getKey().getCode();
            List<String> includedData = AmazonIncludedDataEnum.getAllWithoutVendor();
            String locale = null;
            String sellerId = null;
            List<String> keywords = null;
            List<String> brandNames = null;
            List<String> classificationIds = null;
            int pageSize = size;
            String pageToken = null;
            String keywordsLocale = null;
            ApiResponse<ItemSearchResults> apiResponse;
            try {
                apiResponse = catalogApi.searchCatalogItemsWithHttpInfo(marketplaceIds, identifiers, identifiersType, includedData, locale, sellerId, keywords, brandNames, classificationIds, pageSize, pageToken, keywordsLocale);
            } catch (ApiException e) {
                if (429 == e.getCode()) {
                    // 设置动态速率，失效时间=1/limit
                    BigDecimal timeOut = BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN);
                    redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                }
                throw new ServiceException("[Amazon SP-APi] 下载listing失败:body=" + JSONUtil.toJsonStr(e));
            }
            List<Item> items = apiResponse.getData().getItems();
            if (CollectionUtils.isEmpty(items)) {
                continue;
            }
            for (Item item : items) {
                JSONObject jsonObject = (JSONObject) JSON.toJSON(item);
                String productId = item.getKeyByIdentifierType(entry.getKey(), marketPlaceEnum);
                jsonObject.put("productId", productId);
                resultList.add(jsonObject);
            }
        }
        return resultList;
    }
}
