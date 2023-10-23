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
import com.sdk.oms.shopee.service.ShopeeProductService;
import io.seata.common.util.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.AMAZON)
@BusinessType(BusinessTypeEnum.ORDER)
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
        System.out.println("lastTime:" + lastTime);
        LocalDateTime nextTime = data.getNextTime();
        System.out.println("nextTime:" + nextTime);
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
        List<PlatformProductDTO> productDTOS = new ArrayList<>();
        shopeeShop.getData().forEach(shopAuthEntity -> {
            ApiResult<ShopAuthEntity> shopeeShopById = shopeeFiegn.getShopeeShopById(shopAuthEntity.getShopId());
            if (Objects.nonNull(shopeeShopById) && Objects.nonNull(shopeeShopById.getData())) {
                ShopAuthEntity shop = shopeeShopById.getData();
                List<PlatformProductDTO> list = shopeeProductService.getAllProduct(cfgAppClient.getUrl(), shop.getAccessToken(),
                        Long.parseLong(shop.getShopId()), Long.parseLong(cfgAppClient.getClientId()), cfgAppClient.getClientSecret());
                productDTOS.addAll(list);
            }
        });
        // 返回下载源数据
        return productDTOS.stream()
                .map(e -> new PlatformShopeeListingDTO(e, data))
                .collect(Collectors.toList());
    }


    @Override
    public List<PlatformProductDTO> convert(List<PlatformShopeeListingDTO> sourceDataList) {
        System.out.println("sourceDataList = " + sourceDataList);
        // 订单转换为发送mq数据
        List<PlatformProductDTO> productDTOS = new ArrayList<>(sourceDataList.size());
        sourceDataList.forEach(platformShopeeListingDTO -> {
            productDTOS.add(platformShopeeListingDTO.getPlatformProductDTO());
        });
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return productDTOS;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.SHOPEE.getCode();
    }
}
