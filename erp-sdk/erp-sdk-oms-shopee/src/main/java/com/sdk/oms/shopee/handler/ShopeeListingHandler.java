package com.sdk.oms.shopee.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractProductHandler;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.sdk.oms.shopee.dto.PlatformShopeeListingDTO;
import com.sdk.oms.shopee.dto.product.request.ProductRequest;
import com.sdk.oms.shopee.dto.product.response.ItemInfo;
import com.sdk.oms.shopee.service.ShopeeProductService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 亚马逊产品处理器
 *
 * @Author Cloud
 * @Date 2023/8/31 15:48
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.SHOPEE)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class ShopeeListingHandler extends AbstractProductHandler<PlatformShopeeListingDTO, PlatformProductDTO> {
    @Resource
    private ShopeeFeign shopeeFiegn;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopeeProductService shopeeProductService;

    @Override
    public List<PlatformShopeeListingDTO> download(JobTaskDTO data) {
        LocalDateTime lastTime = data.getLastTime();
        long timeFrom = Timestamp.valueOf(lastTime).getTime() / 1000;
        log.info("lastTime:{},timeFrom:{}", lastTime, timeFrom);
        LocalDateTime nextTime = data.getNextTime();
        long timeTo = Timestamp.valueOf(nextTime).getTime() / 1000;
        log.info("nextTime:{},timeTo:{}", nextTime, timeTo);
        //获取主店铺token
        //根据主店铺获取子店铺token
        ApiResult<List<ShopAuthEntity>> shopeeShop = shopeeFiegn.getShopeeShopList("shopee_shop");
        if (CollectionUtils.isEmpty(shopeeShop.getData())) {
            return Collections.emptyList();
        }
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            return Collections.emptyList();
        }
        List<ItemInfo> itemInfos = new ArrayList<>();
        shopeeShop.getData().forEach(shopAuthEntity -> {
            ApiResult<ShopAuthEntity> shopeeShopById = shopeeFiegn.getShopeeShopById(shopAuthEntity.getShopId());
            if (Objects.nonNull(shopeeShopById) && Objects.nonNull(shopeeShopById.getData())) {
                ShopAuthEntity shop = shopeeShopById.getData();

//                long timest = System.currentTimeMillis() / 1000L;
//                Long time_from = timest - (3600 * 24 * 14);
//                Long time_to = timest;

                ProductRequest productRequest = ProductRequest.builder()
                        .host(cfgAppClient.getUrl())
                        .offset(0)
                        .token(shop.getAccessToken())
                        .shopId(Long.parseLong(shop.getShopeeId()))
                        .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                        .tmpPartnerKey(cfgAppClient.getClientSecret())
//                        .timeFrom(null)
                        .timeFrom(timeFrom)
                        .timeTo(timeTo)
//                        .timeTo(null)
                        .build();
                List<ItemInfo> list = new ArrayList<>();
                try {
                    shopeeProductService.getAllProduct(productRequest, list);
                }catch (Exception e){
                    log.error("获取产品数据异常:{}", e.getMessage());
                }
                if (CollectionUtils.isNotEmpty(list)) {
                    itemInfos.addAll(list);
                }
            }
        });
        // 返回下载源数据
        return itemInfos.stream()
                .map(e -> new PlatformShopeeListingDTO(e, data))
                .collect(Collectors.toList());
    }


    @Override
    public List<PlatformProductDTO> convert(List<PlatformShopeeListingDTO> sourceDataList) {
        if (CollectionUtils.isEmpty(sourceDataList)){
            return Collections.emptyList();
        }
        return sourceDataList.stream()
                .map(PlatformShopeeListingDTO::convertDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.SHOPEE.getCode();
    }
}
