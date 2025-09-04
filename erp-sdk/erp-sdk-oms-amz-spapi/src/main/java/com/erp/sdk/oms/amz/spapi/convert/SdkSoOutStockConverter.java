package com.erp.sdk.oms.amz.spapi.convert;

import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PlatformSoOutStockDetailDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.dto.ReportFulfilledShipmentsMongoDTO;
import jnr.ffi.annotations.In;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 亚马逊物流销售转换器
 *
 * @author Jim
 * @since 2024-03-06
 **/
@Mapper
@Component
public interface SdkSoOutStockConverter {

    SdkSoOutStockConverter INSTANCE = Mappers.getMapper(SdkSoOutStockConverter.class);


    @Mappings({
            @Mapping(target = "uniqueId", source = "sourceDTO.shipmentItemId"),
            @Mapping(target = "downloadTime", expression = "java(java.time.LocalDateTime.now().toString())"),
            @Mapping(target = "handleStatus", source = "handleStatus"),
            @Mapping(target = "shopId", source = "shopId"),
            @Mapping(target = "groupId", source = "groupId"),
            @Mapping(target = "downloadStatus", source = "downloadStatus"),
            @Mapping(target = "salesChannel", source = "salesChannel"),
    })
    PlatformAmazonFulfilledShipmentsDTO sourceDtoToOutStockDto(ReportFulfilledShipmentsMongoDTO sourceDTO,
                                                               String reportId,
                                                               String shopId,
                                                               String groupId,
                                                               String handleStatus,
                                                               Integer downloadStatus,
                                                               String salesChannel
    );


    @Mappings({
            @Mapping(target = "platformCode", source = "platformCode"),
            @Mapping(target = "dictPlatform", constant = "Amazon"),
            @Mapping(target = "platform", constant = "Amazon"),
            @Mapping(target = "shopId", source = "shopId"),
            @Mapping(target = "uniqueId", source = "uniqueId"),
            @Mapping(target = "salesChannel", source = "salesChannel"),
            @Mapping(target = "detailList", expression = "java(sourceDetails.stream().map(INSTANCE::amazonConvertDetailDTO).collect(java.util.stream.Collectors.toList()))"),
    })
    PlatformSoOutStockDTO amazonConvertDTO(String platformCode, String shopId, String uniqueId, List<PlatformAmazonFulfilledShipmentsDTO> sourceDetails, String warehouseId, String warehouseName, String fulfillmentCenter, String salesChannel);


    @Mappings({
            @Mapping(target = "platformDetailId", source = "shipmentItemId"),
            @Mapping(target = "platformCode", source = "amazonOrderId"),
            @Mapping(target = "platformOrderDetailId", source = "amazonOrderItemId"),
            @Mapping(target = "trackNo", source = "trackingNumber"),
            @Mapping(target = "platformOrderCreateTime", source = "purchaseDateLocale"),
            @Mapping(target = "platformPayTime", source = "paymentsDateLocale"),
            @Mapping(target = "platformDeliveryTime", source = "shipmentDateLocale"),
            @Mapping(target = "qtyShipped", source = "quantityShipped"),
            @Mapping(target = "warehouseId", source = "warehouseId"),
            @Mapping(target = "warehouseName", source = "warehouseName"),
            @Mapping(target = "warehouseOrgId", source = "warehouseOrgId"),
            @Mapping(target = "warehouseOrgName", source = "warehouseOrgName"),
            @Mapping(target = "fulfillmentCenterId", source = "fulfillmentCenterId"),
            @Mapping(target = "merchantOrderId", source = "merchantOrderId"),
            @Mapping(target = "merchantOrderItemId", source = "merchantOrderItemId"),
    })
    PlatformSoOutStockDetailDTO amazonConvertDetailDTO(PlatformAmazonFulfilledShipmentsDTO sourceDetail);
}
