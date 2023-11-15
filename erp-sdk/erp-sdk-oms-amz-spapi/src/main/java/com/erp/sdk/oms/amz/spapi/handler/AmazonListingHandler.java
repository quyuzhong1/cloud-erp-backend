package com.erp.sdk.oms.amz.spapi.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractProductHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.api.CatalogApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.convert.SdkListingConverter;
import com.erp.sdk.oms.amz.spapi.csv.ReportListingCsvEntity;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonListingDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportInfoMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportListingMongoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonIncludedDataEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.Item;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.ItemAttributes;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.ItemDimensions;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.ItemDimensionsByMarketplace;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
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
    private ShopInfoFeign shopInfoFeign;


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
        return PlatformDictEnum.AMAZON.getCode();
    }

    @Override
    public PlatformAmazonListingDTO downloadDetail(PlatformAmazonListingDTO dto, JSONObject extendObj) {
        String shopId = extendObj.getString("shopId");
        // 获取店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
        if (null == shopInfoEntity) {
            throw new ServiceException("未找到店铺详情:" + shopId);
        }
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoEntity.getDictCountryCode());

        // 产品规格信息
        String productSpec = "";
        // 包装信息
        String packing = "";
        try {
            // 查询商品详情
            CatalogApi catalogApi = CatalogApi.initApi(marketPlaceEnum);
            String asin = dto.getProductId();
            List<String> marketplaceIds = Collections.singletonList(marketPlaceEnum.getMarketplaceId());
            List<String> includedData = AmazonIncludedDataEnum.getAllWithoutVendor();
            Item response = catalogApi.getCatalogItem(asin, marketplaceIds, includedData, null);
            ItemAttributes attributes = response.getAttributes();
            if (null != attributes) {
                Map<String, Object> tempMap = BeanUtil.beanToMap(attributes);
                if (!tempMap.isEmpty()) {
                    productSpec = tempMap.entrySet().stream()
                            .map(e -> StrUtil.format("{}:{}", e.getKey(), e.getValue().toString()))
                            .collect(Collectors.joining(","));
                }
            }
            ItemDimensions dimensions = response.getDimensions();
            if (null != dimensions) {
                packing = dimensions.stream()
                        .map(ItemDimensionsByMarketplace::combineStr)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.joining(","));
            }
        } catch (ApiException e) {
            throw new ServiceException("[Amazon SP-APi] 下载listing失败" + e);
        }
        // TODO
//        // 产品规格信息
//        dto.setProductSpec(productSpec);
//        // 产品包装信息
//        dto.setProductPacking(packing);

        return dto;
    }
}
