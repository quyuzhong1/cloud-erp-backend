package com.sdk.wms.goodcang.convert;

import com.common.business.dto.*;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.business.utils.MD5Util;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.sdk.wms.goodcang.dto.response.*;
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
public interface GoodCangConverter {

    GoodCangConverter INSTANCE = Mappers.getMapper(GoodCangConverter.class);

    @Mappings({
            @Mapping(target = "platformType", constant = "warehouse"),
            @Mapping(target = "platformSkuNo", source = "productSku"),
            @Mapping(target = "platformSkuName", source = "productTitleCn"),
            @Mapping(target = "productImageUrl", source = "productLink"),
            @Mapping(target = "productSpec", source = "productModel"),
            @Mapping(target = "type", expression ="java(GoodCangConverter.getType())"),
            @Mapping(target = "platformUpdateTime", source = "productModifyTime"),
            @Mapping(target = "downloadTime", expression = "java(GoodCangConverter.getNowTime())"),
            @Mapping(target = "uniqueId", expression = "java(GoodCangConverter.getUniqueKey(sourceData))"),
            @Mapping(target = "matchResult", constant = "false"),
            @Mapping(target = "platform", expression = "java(GoodCangConverter.getProvider())")
    })
    PlatformProductDTO productConversion(GoodCangSkuResp sourceData);
    List<PlatformProductDTO> productConversion(List<GoodCangSkuResp> sourceDataList);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(GoodCangConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "warehouseName",  source = "warehouseName"),
            @Mapping(target = "countryCode",  source = "countryCode"),
            @Mapping(target = "providerErpId",  source = "authId")
    })
    PlatformWarehouseDTO warehouseConversion(GoodCangWarehouseResp sourceData);
    List<PlatformWarehouseDTO> warehouseConversion(List<GoodCangWarehouseResp> sourceDataList);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(GoodCangConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "providerErpId",  source = "authId")
    })
    PlatformTransferWarehouseDTO transferWarehouseConversion(GoodCangTransferWarehouseResp sourceDataList);
    List<PlatformTransferWarehouseDTO> transferWarehouseConversion(List<GoodCangTransferWarehouseResp> sourceDataList);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(GoodCangConverter.getWarehousePlatformType())"),
            @Mapping(target = "provider",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "platform",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "providerErpId",  source = "authId"),
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "platformWarehouseCode",  source = "warehouseCode"),
            @Mapping(target = "platformWarehouseName",  source = "warehouseDesc"),
            @Mapping(target = "downloadTime", expression = "java(java.time.LocalDateTime.now())"),
    })
    PlatformInventoryDTO inventoryConversion(GoodCangInventoryResp sourceData);
    List<PlatformInventoryDTO> inventoryConversion(List<GoodCangInventoryResp> sourceDataList);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(GoodCangConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "receivingCode",  source = "receivingCode"),
            @Mapping(target = "receivingStatus",  expression = "java(com.sdk.wms.goodcang.enums.GoodCangEnums.OpenReceivingStatusEnum.getInstockByCode(sourceData.getReceivingStatus()))"),
            @Mapping(target = "downloadTime", expression = "java(java.time.LocalDateTime.now())"),
            @Mapping(target = "hasReceivedData", constant = "false"),
            @Mapping(target = "receivingDataList",  source = "gcReceivingDataList"),
    })
    PlatformInboundDTO inboundConversion(GoodCangReceiptBatchResp sourceData);
    List<PlatformInboundDTO> inboundConversion(List<GoodCangReceiptBatchResp> sourceDataList);

    @Mappings({
            @Mapping(target = "productSku", source = "productSku"),
            @Mapping(target = "receiveQty",  source = "receivedQty"),
            @Mapping(target = "receiveTime",  expression = "java(java.time.LocalDateTime.now())"),
    })
    PlatformInboundDTO.Receiving inboundReceiveConversion(GoodCangReceiptBatchResp.GcReceiving data);

    @Mappings({
            @Mapping(target = "warehousePlatformType", expression = "java(GoodCangConverter.getWarehousePlatformType())"),
            @Mapping(target = "platform",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "provider",  expression = "java(GoodCangConverter.getProvider())"),
            @Mapping(target = "orderCode",  source = "orderCode"),
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "outBoundTime",  source = "outBoundTime"),
            @Mapping(target = "trackNo",  source = "trackNo"),
            @Mapping(target = "orderStatus",  expression = "java(com.sdk.wms.goodcang.enums.GoodCangEnums.OrderStatusEnum.getErpOrderStatus(sourceData.getOrderStatus()))"),
            @Mapping(target = "thirdOrderStatus",  expression = "java(com.sdk.wms.goodcang.enums.GoodCangEnums.OrderStatusEnum.getName(sourceData.getOrderStatus()))"),
            @Mapping(target = "abnormalProblemReason",  source = "abnormalProblemReason"),
    })
    PlatformOutboundDTO outboundConversion(GoodCangOutboundResp sourceData);
    List<PlatformOutboundDTO> outboundConversion(List<GoodCangOutboundResp> sourceDataList);

    static String getNowTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    static String getType(){
        return RuleTypeEnum.WAREHOUSE.getCode();
    }

    static String getUniqueKey(GoodCangSkuResp sourceData){
        return MD5Util.toMD5(getProvider()+sourceData.getProductSku());
    }

    static String getProvider(){
        return OmsPlatformEnum.OMS_GOOD_CANG.getCode();
    }

    static String getWarehousePlatformType(){
        return WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.getCode();
    }
}
