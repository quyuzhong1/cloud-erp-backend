package com.erp.server.oms.convert;

import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.dto.SoMultiChannelDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface SoMultiChannelConverter {

    SoMultiChannelConverter INSTANCE = Mappers.getMapper(SoMultiChannelConverter.class);

    @Mappings({
            @Mapping(target = "billStatus", ignore = true),
            @Mapping(target = "deliveryCode", ignore = true),
            @Mapping(target = "deliveryShopId", source = "shopInfoEntity.id"),
            @Mapping(target = "deliveryShopName", source = "shopInfoEntity.name"),
            @Mapping(target = "shopId", source = "soB2cEntity.shopId"),
            @Mapping(target = "shopName", source = "soB2cEntity.shopName"),
            @Mapping(target = "deliveryStatus", ignore = true),
            @Mapping(target = "deliveryTime", ignore = true),
            @Mapping(target = "platformCode", source = "soB2cEntity.platformCode"),
            @Mapping(target = "shipmentCode", ignore = true),
            @Mapping(target = "signOrderError", ignore = true),
            @Mapping(target = "soCode", source = "soB2cEntity.code"),
            @Mapping(target = "soId", source = "soB2cEntity.id"),
            @Mapping(target = "remark", source = "dto.remark"),
            @Mapping(target = "trackNo", ignore = true),
            @Mapping(target = "deliveryPlatform", source = "shopInfoEntity.dictPlatform"),
            @Mapping(target = "dictPlatform", source = "soB2cEntity.dictPlatform"),
            @Mapping(target = "logisticsChannelId", source = "channelEntity.id"),
            @Mapping(target = "logisticsChannelName", source = "channelEntity.name"),
            @Mapping(target = "deliveryWarehouseId", source = "shopInfoEntity.warehouseId"),
            @Mapping(target = "deliveryWarehouseName", source = "shopInfoEntity.warehouseName"),
            @Mapping(target = "shippingMethod", source = "dto.shippingMethod")
    })
    SoMultiChannelDTO.AddDTO soB2cToAddDTO(SoMultiChannelDTO.SaveDTO dto, ShopInfoEntity shopInfoEntity, SoB2cEntity soB2cEntity, LogisticsChannelEntity channelEntity);
    @Mappings({
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "qty", source = "deliveryQty"),
            @Mapping(target = "deliveryQty", ignore = true),
            @Mapping(target = "outstockStatus", ignore = true)
    })
    SoMultiChannelDetailDTO.AddDTO soB2cDetailToAddDTO(SoMultiChannelDTO.SoViewDTO detail);
    List<SoMultiChannelDetailDTO.AddDTO> soB2cDetailToAddDTO(List<SoMultiChannelDTO.SoViewDTO> detailList);

    @Mappings({
            @Mapping(target = "actualDeliveryDate", source = "soMultiChannelEntity.deliveryTime"),
            @Mapping(target = "soId", source = "soB2cEntity.id"),
            @Mapping(target = "soCode", source = "soB2cEntity.code"),
            @Mapping(target = "shopId", source = "soB2cEntity.shopId"),
            @Mapping(target = "dictPlatform", source = "soB2cEntity.dictPlatform"),
            @Mapping(target = "logisticsChannelId", source = "soMultiChannelEntity.logisticsChannelId"),
            @Mapping(target = "logisticsChannelName", source = "soMultiChannelEntity.logisticsChannelName"),
            @Mapping(target = "billDate", source = "soB2cEntity.billDate"),
            @Mapping(target = "sourceId", source = "thirdWarehouseDeliveryEntity.id"),
            @Mapping(target = "sourceCode", source = "thirdWarehouseDeliveryEntity.code"),
            @Mapping(target = "batchNo", ignore = true),
            @Mapping(target = "carrierId", ignore = true),
            @Mapping(target = "checkSkuHistory", ignore = true),
            @Mapping(target = "country", source = "soB2cReceiverEntity.country"),
            @Mapping(target = "customerId", source = "customerInfoEntity.id"),
            @Mapping(target = "customerName", source = "customerInfoEntity.name"),
            @Mapping(target = "detailList", ignore = true),
            @Mapping(target = "hasPlatformWarehouseOrder", ignore = true),
            @Mapping(target = "orderType", expression = "java(com.common.business.enums.OrderTypeEnum.B2C.getCode())"),
            @Mapping(target = "planDeliveryDate", ignore = true),
            @Mapping(target = "salesDeptId", source = "customerInfoEntity.salesDeptId"),
            @Mapping(target = "salesOrgId", source = "customerInfoEntity.useOrgId"),
            @Mapping(target = "salesOrgName", source = "customerInfoEntity.useOrgName"),
            @Mapping(target = "sellerId", source = "customerInfoEntity.sellerId"),
            @Mapping(target = "sellerName", source = "customerInfoEntity.sellerName"),
            @Mapping(target = "transportNo", source = "soMultiChannelEntity.trackNo"),
            @Mapping(target = "trackNo", source = "soMultiChannelEntity.trackNo"),
            @Mapping(target = "warehouseId", source = "soB2cDetailEntity.warehouseId"),
            @Mapping(target = "warehouseName", source = "soB2cDetailEntity.warehouseName"),
            @Mapping(target = "warehouseOrgId", source = "soB2cDetailEntity.warehouseOrgId"),
            @Mapping(target = "warehouseOrgName", source = "soB2cDetailEntity.warehouseOrgName")
    })
    SoOutstockDTO.GenerateB2cDTO convertSoOutstockGenerateB2cDTO(SoMultiChannelEntity soMultiChannelEntity, SoB2cEntity soB2cEntity, ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity, SoB2cDetailEntity soB2cDetailEntity, CustomerInfoEntity customerInfoEntity, SoB2cReceiverEntity soB2cReceiverEntity);
    @Mappings({
            @Mapping(target = "skuId", source = "thirdWarehouseDeliveryDetailEntity.skuId"),
            @Mapping(target = "skuNo", source = "thirdWarehouseDeliveryDetailEntity.skuNo"),
            @Mapping(target = "warehouseId", source = "detailEntity.warehouseId"),
            @Mapping(target = "warehouseName", source = "detailEntity.warehouseName"),
            @Mapping(target = "virtualWarehouseId", source = "detailEntity.virtualWarehouseId"),
            @Mapping(target = "price", source = "detailEntity.price"),
            @Mapping(target = "amount", source = "detailEntity.amount"),
            @Mapping(target = "taxRate", source = "detailEntity.taxRate"),
            @Mapping(target = "exchangeRate", source = "detailEntity.exchangeRate"),
            @Mapping(target = "currency", source = "detailEntity.currency"),
            @Mapping(target = "soDetailId", source = "detailEntity.id"),
            @Mapping(target = "sourceDetailId", source = "thirdWarehouseDeliveryDetailEntity.id"),
            @Mapping(target = "actualQty", ignore = true),
            @Mapping(target = "attachNameList", ignore = true),
            @Mapping(target = "attachUrlList", ignore = true),
            @Mapping(target = "historySkuMappingList", ignore = true),
            @Mapping(target = "planQty", source = "thirdWarehouseDeliveryDetailEntity.deliveryQty"),
            @Mapping(target = "platformCode", ignore = true),
            @Mapping(target = "platformDetailId", ignore = true),
            @Mapping(target = "platformSoDetailId", ignore = true),
            @Mapping(target = "remark", ignore = true)
    })
    SoOutstockDetailDTO.AddDTO convertSoOutstockGenerateB2cDetailDTO(ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity, SoB2cDetailEntity detailEntity);
}
