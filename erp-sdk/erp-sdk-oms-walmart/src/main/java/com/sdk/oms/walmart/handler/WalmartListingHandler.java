package com.sdk.oms.walmart.handler;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractProductHandler;
import com.common.core.utils.UUID;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.PlatformWalmartListingDTO;
import com.sdk.oms.walmart.dto.WalmartShopInfoDTO;
import com.sdk.oms.walmart.dto.WalmartTokenDTO;
import com.sdk.oms.walmart.service.WalmartSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 沃尔玛商品信息
 * @Author Luo_WG
 * @Date 2023/10/18 11:24
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.WALMART)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class WalmartListingHandler extends AbstractProductHandler<PlatformWalmartListingDTO, PlatformProductDTO> {

    @Resource
    private  WalmartSdkClientService walmartSdkClientService;

    @Override
    public List<PlatformWalmartListingDTO> download(JobTaskDTO data) {
        //  根据店铺ID获取授权
        WalmartShopInfoDTO tokenDTO = WalmartSdkClientService.getTokenByShopId(data.getShopId());
        if (null == tokenDTO){
            log.error("[Walmart产品下载]从缓存中获取Walmart token 失败: shopId={}", data.getShopId());
            return Collections.emptyList();
        }
        //获取令牌
        String baseUrl = WalmartStaticKey.baseUrl + "token";

        WalmartTokenDTO walmartTokenDTO = walmartSdkClientService.sendWalmartPostToken(baseUrl, tokenDTO.getClientId(), tokenDTO.getClientSecret());

        baseUrl = WalmartStaticKey.baseUrl + data.getApiCode();
        //拉取数据
        String date = walmartSdkClientService.sendWalmartGet(baseUrl, tokenDTO.getClientId(), tokenDTO.getClientSecret(), walmartTokenDTO.getAccessToken());
/*
        WalmartTokenDTO tokenDTO = JSONUtil.toBean(date, WalmartTokenDTO.class);

        if (CollectionUtils.isEmpty(products.values())) {
            return Collections.emptyList();
        }
        // 返回下载源数据
        return products.values().stream()
                .map(e -> new PlatformShopifyListingDTO(e, data))
                .collect(Collectors.toList());
        */
        return null;
    }

    @Override
    public List<PlatformProductDTO> convert(List<PlatformWalmartListingDTO> sourceDataList) {
       /* // Shopify商品转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 已发布并售卖中
                .filter(e -> e.getShopifyProduct().isPublished() && "active".equalsIgnoreCase(e.getShopifyProduct().getStatus()))
                // 组装
                .map(PlatformShopifyListingDTO::convertDTO)
                .flatMap(List::stream).collect(Collectors.toList());*/
        return null;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.SHOPIFY.getCode();
    }


}
