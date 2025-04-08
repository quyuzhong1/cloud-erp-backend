package com.sdk.oms.mercadolocal.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.mercadolocal.dto.MercadoOrderDTO;
import com.sdk.oms.mercadolocal.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.order.OrderViewDTO;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.MERCADOLIBRE_LOCAL)
@BusinessType(BusinessTypeEnum.ORDER)
public class MercadoOrderHandler extends AbstractOrderHandler<MercadoOrderDTO, PlatformOrderDTO> {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private MercadoLocalSdkClientService mercadoLocalSdkClientService;

    @Override
    public List<MercadoOrderDTO> download(JobTaskDTO task) {
        // Shopify订单下载
        MercadoShopInfoDTO shopInfoDTO = mercadoLocalSdkClientService.getShopInfoByShopId(task.getShopId());
        if (null == shopInfoDTO) {
            log.error("[美客多订单下载]从缓存中获取美客多 token 失败: shopId={}", task.getShopId());
            return Collections.emptyList();
        }


        //发送请求
        List<OrderViewDTO> orders = mercadoLocalSdkClientService.sendMercadoGetOrder(shopInfoDTO, task);

        // 返回下载源数据
        return orders.stream()
                .map(e -> new MercadoOrderDTO(e, task, shopInfoDTO.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformOrderDTO> convert(List<MercadoOrderDTO> sourceDataList) {
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(MercadoOrderDTO::convertDTO).collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }


}

