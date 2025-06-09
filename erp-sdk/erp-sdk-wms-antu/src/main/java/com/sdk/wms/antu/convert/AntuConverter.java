package com.sdk.wms.antu.convert;

import com.common.business.dto.*;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.business.utils.MD5Util;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.sdk.wms.antu.dto.response.*;
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
public interface AntuConverter {

    AntuConverter INSTANCE = Mappers.getMapper(AntuConverter.class);

    @Mappings({
            @Mapping(target = "platformType", constant = "warehouse"),
            @Mapping(target = "platformSkuNo", source = "productSku"),
            @Mapping(target = "platformSkuName", source = "productTitle"),
            @Mapping(target = "platformProductBarcode", source = "productId"),
            @Mapping(target = "productImageUrl", source = "productDescUrl"),
            @Mapping(target = "productSpec", source = "productModel"),
            @Mapping(target = "type", expression ="java(AntuConverter.getType())"),
            @Mapping(target = "platformUpdateTime", source = "productModifyTime"),
            @Mapping(target = "downloadTime", expression = "java(AntuConverter.getNowTime())"),
            @Mapping(target = "uniqueId", expression = "java(AntuConverter.getUniqueKey(sourceData))"),
            @Mapping(target = "matchResult", constant = "false"),
            @Mapping(target = "platform", constant = "antu")
    })
    PlatformProductDTO productConversion(AntuProductResp sourceData);
    List<PlatformProductDTO> productConversion(List<AntuProductResp> sourceDataList);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(AntuConverter.getWarehousePlatformType())"),
            @Mapping(target = "provider",  expression = "java(AntuConverter.getProvider())"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "warehouseName",  source = "warehouseName"),
            @Mapping(target = "countryCode",  source = "countryCode"),
            @Mapping(target = "providerErpId",  source = "authId"),
            @Mapping(target = "platform",  expression = "java(AntuConverter.getProvider())")
    })
    PlatformWarehouseDTO warehouseConversion(AntuWarehouseResp sourceData);
    List<PlatformWarehouseDTO> warehouseConversion(List<AntuWarehouseResp> sourceDataList);

    @Mappings({
            @Mapping(target = "provider",  expression = "java(AntuConverter.getProvider())"),
            @Mapping(target = "regionId",  source = "regionId"),
            @Mapping(target = "parentRegionId",  source = "parentRegionId"),
            @Mapping(target = "regionName",  source = "regionName"),
            @Mapping(target = "regionLevel",  source = "regionLevel"),
            @Mapping(target = "platform",  expression = "java(AntuConverter.getProvider())")
    })
    PlatformCityDictDTO regionConversion(AntuRegionResp sourceData);
    List<PlatformCityDictDTO> regionConversion(List<AntuRegionResp> sourceDataList);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(AntuConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(AntuConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(AntuConverter.getProvider())"),
            @Mapping(target = "receivingCode",  source = "receivingCode"),
            @Mapping(target = "receivingStatus",  expression = "java(com.sdk.wms.antu.enums.AntuEnums.ReceivingStatusEnum.getInstockByCode(sourceData.getReceivingStatus()))"),
            @Mapping(target = "items",  source = "items"),
            @Mapping(target = "downloadTime", expression = "java(java.time.LocalDateTime.now())"),
    })
    PlatformInboundDTO inboundConversion(AntuReceiptResp sourceData);
    List<PlatformInboundDTO> inboundConversion(List<AntuReceiptResp> sourceDataList);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "receivedQuantity",  source = "receivedQuantity"),
            @Mapping(target = "putawayQuantity",  source = "putawayQuantity"),
            @Mapping(target = "boxNo",  source = "boxNo"),
    })
    PlatformInboundDTO.Item inboundConversion(AntuReceiptResp.Item item);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(AntuConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(AntuConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(AntuConverter.getProvider())"),
            @Mapping(target = "providerErpId",  source = "authId"),
            @Mapping(target = "transferOnway",  source = "onway"),
            @Mapping(target = "platformWarehouseCode",  source = "warehouseCode"),
            @Mapping(target = "onway",  ignore = true),
            @Mapping(target = "downloadTime", expression = "java(java.time.LocalDateTime.now())")
    })
    PlatformInventoryDTO inventoryConversion(AntuInventoryResp sourceData);
    List<PlatformInventoryDTO> inventoryConversion(List<AntuInventoryResp> sourceDataList);

    static String getNowTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    static String getType(){
        return RuleTypeEnum.WAREHOUSE.getCode();
    }

    static String getUniqueKey(AntuProductResp sourceData){
        return MD5Util.toMD5("antu"+sourceData.getProductSku());
    }

    static String getProvider(){
        return OmsPlatformEnum.OMS_IML.getCode();
    }

    static String getWarehousePlatformType(){
        return WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode();
    }
    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(AntuConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(AntuConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(AntuConverter.getProvider())"),
            @Mapping(target = "orderCode",  source = "orderCode"),
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "outBoundTime",  source = "outBoundTime"),
            @Mapping(target = "trackNo",  source = "trackNo"),
            @Mapping(target = "orderStatus",  expression = "java(com.sdk.wms.antu.enums.AntuEnums.OrderStatusEnum.getErpOrderStatus(sourceData.getOrderStatus()))"),
            @Mapping(target = "thirdOrderStatus",  expression = "java(com.sdk.wms.antu.enums.AntuEnums.OrderStatusEnum.getName(sourceData.getOrderStatus()))"),
            @Mapping(target = "abnormalProblemReason",  source = "abnormalReason"),
    })
    PlatformOutboundDTO outboundConversion(AntuOutboundResp sourceData);
    List<PlatformOutboundDTO> outboundConversion(List<AntuOutboundResp> sourceDataList);
}
