package com.sdk.wms.tongyou.convert;

import com.common.business.dto.*;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.business.utils.MD5Util;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.sdk.wms.tongyou.dto.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
@Component
public interface TongYouConverter {

    TongYouConverter INSTANCE = Mappers.getMapper(TongYouConverter.class);

    @Mappings({
            @Mapping(target = "platformType", constant = "warehouse"),
            @Mapping(target = "platformSkuNo", source = "productSku"),
            @Mapping(target = "platformSkuName", source = "productTitle"),
            @Mapping(target = "productImageUrl", source = "productDescUrl"),
            @Mapping(target = "productSpec", source = "productModel"),
            @Mapping(target = "type", expression ="java(TongYouConverter.getType())"),
            @Mapping(target = "platformUpdateTime", source = "productModifyTime"),
            @Mapping(target = "downloadTime", expression = "java(TongYouConverter.getNowTime())"),
            @Mapping(target = "uniqueId", expression = "java(TongYouConverter.getUniqueKey(sourceData))"),
            @Mapping(target = "matchResult", constant = "false"),
            @Mapping(target = "platform", constant = "tongyou")
    })
    PlatformProductDTO productConversion(TongYouProductResp sourceData);
    List<PlatformProductDTO> productConversion(List<TongYouProductResp> sourceDataList);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(TongYouConverter.getWarehousePlatformType())"),
            @Mapping(target = "provider",  expression = "java(TongYouConverter.getProvider())"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "warehouseName",  source = "warehouseName"),
            @Mapping(target = "countryCode",  source = "countryCode"),
            @Mapping(target = "providerErpId",  source = "authId"),
            @Mapping(target = "platform",  expression = "java(TongYouConverter.getProvider())")
    })
    PlatformWarehouseDTO warehouseConversion(TongYouWarehouseResp sourceData);
    List<PlatformWarehouseDTO> warehouseConversion(List<TongYouWarehouseResp> sourceDataList);

    @Mappings({
            @Mapping(target = "provider",  expression = "java(TongYouConverter.getProvider())"),
            @Mapping(target = "regionId",  source = "regionId"),
            @Mapping(target = "parentRegionId",  source = "parentRegionId"),
            @Mapping(target = "regionName",  source = "regionName"),
            @Mapping(target = "regionLevel",  source = "regionLevel"),
            @Mapping(target = "platform",  expression = "java(TongYouConverter.getProvider())")
    })
    PlatformCityDictDTO regionConversion(TongYouRegionResp sourceData);
    List<PlatformCityDictDTO> regionConversion(List<TongYouRegionResp> sourceDataList);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(TongYouConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(TongYouConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(TongYouConverter.getProvider())"),
            @Mapping(target = "receivingCode",  source = "receivingCode"),
            @Mapping(target = "receivingStatus",  expression = "java(com.sdk.wms.tongyou.enums.TongYouEnums.ReceivingStatusEnum.getInstockByCode(sourceData.getReceivingStatus()))"),
            @Mapping(target = "items",  source = "items"),
            @Mapping(target = "downloadTime", expression = "java(java.time.LocalDateTime.now())"),
    })
    PlatformInboundDTO inboundConversion(TongYouReceiptResp sourceData);
    List<PlatformInboundDTO> inboundConversion(List<TongYouReceiptResp> sourceDataList);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "receivedQuantity",  source = "receivedQuantity"),
            @Mapping(target = "putawayQuantity",  source = "putawayQuantity"),
            @Mapping(target = "boxNo",  source = "boxNo"),
    })
    PlatformInboundDTO.Item inboundConversion(TongYouReceiptResp.Item item);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(TongYouConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(TongYouConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(TongYouConverter.getProvider())"),
            @Mapping(target = "providerErpId",  source = "authId"),
            @Mapping(target = "transferOnway",  source = "onway"),
            @Mapping(target = "platformWarehouseCode",  source = "warehouseCode"),
            @Mapping(target = "onway",  ignore = true),
            @Mapping(target = "downloadTime", expression = "java(java.time.LocalDateTime.now())")
    })
    PlatformInventoryDTO inventoryConversion(TongYouInventoryResp sourceData);
    List<PlatformInventoryDTO> inventoryConversion(List<TongYouInventoryResp> sourceDataList);

    static String getNowTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    static String getType(){
        return RuleTypeEnum.WAREHOUSE.getCode();
    }

    static String getUniqueKey(TongYouProductResp sourceData){
        return MD5Util.toMD5("tongyou"+sourceData.getProductSku());
    }

    static String getProvider(){
        return OmsPlatformEnum.TONG_YOU.getCode();
    }

    static String getWarehousePlatformType(){
        return WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode();
    }
    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(TongYouConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(TongYouConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(TongYouConverter.getProvider())"),
            @Mapping(target = "orderCode",  source = "orderCode"),
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "outBoundTime",  source = "outBoundTime"),
            @Mapping(target = "trackNo",  source = "trackNo"),
            @Mapping(target = "orderStatus",  expression = "java(com.sdk.wms.tongyou.enums.TongYouEnums.OrderStatusEnum.getErpOrderStatus(sourceData.getOrderStatus()))"),
            @Mapping(target = "thirdOrderStatus",  expression = "java(com.sdk.wms.tongyou.enums.TongYouEnums.OrderStatusEnum.getName(sourceData.getOrderStatus()))"),
            @Mapping(target = "abnormalProblemReason",  source = "abnormalReason"),
    })
    PlatformOutboundDTO outboundConversion(TongYouOutboundResp sourceData);
    List<PlatformOutboundDTO> outboundConversion(List<TongYouOutboundResp> sourceDataList);
}
