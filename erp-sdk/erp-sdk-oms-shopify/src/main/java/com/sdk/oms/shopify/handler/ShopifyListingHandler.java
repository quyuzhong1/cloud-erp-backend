package com.sdk.oms.shopify.handler;

import cn.hutool.core.util.StrUtil;
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
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyProducts;
import com.sdk.oms.shopify.dto.PlatformShopifyListingDTO;
import com.sdk.oms.shopify.dto.ShopifyTokenDTO;
import io.seata.core.constants.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private ShopifyRestClientService shopifyRestClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<PlatformShopifyListingDTO> download(JobTaskDTO data) {
        //  根据店铺ID获取授权
        ShopifyTokenDTO tokenDTO = parseTokenAndDomain(data.getShopId());
        String shopifyShopDomain = tokenDTO.getShopDomain();
        String accessToken = tokenDTO.getAccessToken();

        // Shopify产品下载所有(SDK已分页查询所有)
        ShopifyProducts products = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken).getProducts();
        if (CollectionUtils.isEmpty(products.values())) {
            return Collections.emptyList();
        }
        // 返回下载源数据
        return products.values().stream()
                .map(e -> new PlatformShopifyListingDTO(e, data))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformProductDTO> convert(List<PlatformShopifyListingDTO> sourceDataList) {
        // Shopify商品转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 已发布并售卖中
                .filter(e -> e.getShopifyProduct().getPublished() && "active".equalsIgnoreCase(e.getShopifyProduct().getStatus()))
                // 组装
                .map(PlatformShopifyListingDTO::convertDTO)
                .flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.SHOPIFY.getCode();
    }


    private ShopifyTokenDTO parseTokenAndDomain(String shopId) {
        // TODO 转移common
        // platform-token:平台名称:店铺ID
        String REDIS_PLATFORM_SHOP_TOKEN = "platform-shop-token:{}:{}";
        String tokenKey = StrUtil.format(REDIS_PLATFORM_SHOP_TOKEN, getTargetPlatform(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof ShopifyTokenDTO) {
                return (ShopifyTokenDTO) tokenObj;
            }
        }
        // 查询

        return null;
    }
}
