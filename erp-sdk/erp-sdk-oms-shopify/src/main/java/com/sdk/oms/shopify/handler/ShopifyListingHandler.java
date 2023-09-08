package com.sdk.oms.shopify.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformDataDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractProductHandler;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyProducts;
import com.sdk.oms.shopify.dto.PlatformShopifyListingDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Shopify产品处理器
 *
 * @Author Jim
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.OMS)
@PlatformType(PlatformDictEnum.SHOPIFY)
@BusinessType(BusinessTypeEnum.ORDER)
public class ShopifyListingHandler extends AbstractProductHandler<PlatformShopifyListingDTO, PlatformProductDTO> {

    @Resource
    private ShopifyRestClientService  shopifyRestClientService;

    @Override
    public List<PlatformShopifyListingDTO> download(JobTaskDTO data) {
        // 获取授权
        String shopifyShopName = "";
        String accessToken = "";
        // Shopify产品下载

        ShopifyProducts products = shopifyRestClientService.getShopifyRestClient(shopifyShopName, accessToken).getProducts();


        // 返回下载源数据
        return null;
    }



    @Override
    public List<PlatformProductDTO> convert(List<PlatformShopifyListingDTO> sourceDataList) {
        // TODO: Shopify订单转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return null;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.SHOPIFY.getCode();
    }
}
