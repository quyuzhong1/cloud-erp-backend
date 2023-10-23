package com.erp.sdk.oms.amz.spapi.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
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
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.api.CatalogApi;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.csv.ListingCsvReportEntity;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonListingDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonIncludedDataEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.Item;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.ItemAttributes;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.ItemDimensions;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.ItemDimensionsByMarketplace;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderItemList;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    public List<PlatformAmazonListingDTO> download(JobTaskDTO data) {
        // 获取店铺信息
        String shopId = data.getShopId();
        ShopInfoEntity shop = shopInfoFeign.getShopInfoById(shopId);
        if (null == shop) {
            throw new ServiceException("未找到店铺信息:shopId=" + shopId);
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shop.getDictCountryCode());

        // 亚马逊商品下载
        // TODO 查询当前店铺是否有最新生成的报告文档ID
        String reportDocumentId = "";
        if (StringUtils.isBlank(reportDocumentId)) {
            return Collections.emptyList();
        }

        // 初始化api
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum);
        try {
            // 根据报告文档ID获取商品报告链接
            ReportDocument reportDocument = reportsApi.getReportDocument(reportDocumentId);

            // 报告链接
            String url = reportDocument.getUrl();
            // 下载报告信息
            List<ListingCsvReportEntity> listingReoprtList = AmazonSpApiReportUtils.downloadAndParseListing(url);

            // 返回下载源数据
            return listingReoprtList.stream()
                    .map(e -> new PlatformAmazonListingDTO(e, shop))
                    .collect(Collectors.toList());
        } catch (ApiException | IOException e) {
            throw new ServiceException("[Amazon SP-APi] 下载listing失败" + e);
        }
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
    public PlatformProductDTO downloadDetail(PlatformProductDTO dto, JSONObject extendObj) {
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
            String asin = dto.getPlatformProductNo();
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
        // 产品规格信息
        dto.setProductSpec(productSpec);
        // 产品包装信息
        dto.setProductPacking(packing);

        return dto;
    }
}
