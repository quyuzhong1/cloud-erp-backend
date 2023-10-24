package com.sdk.oms.shopee.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.sdk.oms.shopee.dto.PlatformShopeeOrderDTO;
import com.sdk.oms.shopee.dto.order.request.OrderRequest;
import com.sdk.oms.shopee.dto.order.response.OrderDetail;
import com.sdk.oms.shopee.service.ShopeeOrderService;
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
 * 亚马逊订单处理器
 *
 * @Author Cloud
 * @Date 2023/8/31 15:48
 **/
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.SHOPEE)
@BusinessType(BusinessTypeEnum.ORDER)
public class ShopeeOrderHandler extends AbstractOrderHandler<PlatformShopeeOrderDTO, PlatformOrderDTO> {

    @Resource
    private ShopeeFeign shopeeFiegn;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopeeOrderService shopeeOrderService;

    @Override
    public List<PlatformShopeeOrderDTO> download(JobTaskDTO data) {
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
        List<OrderDetail> orderDTOS = new ArrayList<>();
        shopeeShop.getData().forEach(shopAuthEntity -> {
            ApiResult<ShopAuthEntity> shopeeShopById = shopeeFiegn.getShopeeShopById(shopAuthEntity.getShopId());
            if (Objects.nonNull(shopeeShopById) && Objects.nonNull(shopeeShopById.getData())) {
//                ShopAuthEntity shop = shopeeShopById.getData();
                OrderRequest orderRequest = OrderRequest.builder()
                        .offset(0)
                        .timeFrom((long) lastTime.getSecond())
                        .timeTo((long) nextTime.getSecond())
                        .tmpPartnerKey(cfgAppClient.getClientSecret())
                        .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                        .token(shopAuthEntity.getAccessToken())
                        .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                        .host(cfgAppClient.getUrl())
                        .cursor("")
                        .build();
                List<OrderDetail> orderDetails = new ArrayList<>();
                shopeeOrderService.getAllOrder(orderRequest,orderDetails);
                if (CollectionUtils.isNotEmpty(orderDetails)){
                    orderDTOS.addAll(orderDetails);
                }

            }
        });
        // 返回下载源数据
        return orderDTOS.stream()
                .map(e -> new PlatformShopeeOrderDTO(e, data))
                .collect(Collectors.toList());
    }


    @Override
    public List<PlatformOrderDTO> convert(List<PlatformShopeeOrderDTO> sourceDataList) {
        //亚马逊订单转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(PlatformShopeeOrderDTO::convertDTO)
//                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.AMAZON.getCode();
    }
}
