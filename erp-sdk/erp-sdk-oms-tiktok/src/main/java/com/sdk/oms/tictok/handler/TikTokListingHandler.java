package com.sdk.oms.tictok.handler;

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
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.tictok.dto.TikTokListingDTO;
import com.sdk.oms.tictok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
@PlatformType(PlatformDictEnum.TIK_TOK)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class TikTokListingHandler extends AbstractProductHandler<TikTokListingDTO, PlatformProductDTO> {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public List<TikTokListingDTO> download(JobTaskDTO data) {
        //  根据店铺ID获取授权
        TikTokListingDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(data.getShopId());
        if (null == shopInfoDTO) {
//            log.error("[美客多商品下载]  获取 token 失败: shopId={}", data.getShopId());
            return Collections.emptyList();
        }

        //发送请求
        List<ListingViewDTO> resultsBeanList = tikTokSdkClientService.sendMercadoGetListing(shopInfoDTO);

        // 返回下载源数据
        return resultsBeanList.stream()
                .map(e -> new MercadoListingDTO(e.getBody(), data))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformProductDTO> convert(List<TikTokListingDTO> sourceDataList) {
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(TikTokListingDTO::convertDTO).collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }
}

