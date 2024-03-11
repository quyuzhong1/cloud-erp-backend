package com.erp.sdk.oms.amz.spapi.convert;

import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PlatformSoOutStockDetailDTO;
import com.erp.sdk.oms.amz.spapi.csv.ReportFulfilledShipmentsCsvEntity;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
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
            @Mapping(target = "uniqueId", source = "csvEntity.shipmentItemId"),
            @Mapping(target = "downloadTime", expression = "java(java.time.LocalDateTime.now().toString())"),
            @Mapping(target = "handleStatus", source = "handleStatus"),
    })
    PlatformAmazonFulfilledShipmentsDTO sourceDtoToOutStockDto(ReportFulfilledShipmentsCsvEntity csvEntity, String reportId, String handleStatus);


    @Mappings({
            @Mapping(target = "platformCode", source = "platformCode"),
            @Mapping(target = "dictPlatform", constant = "Amazon"),
            @Mapping(target = "detailList", expression = "java(sourceDetails.stream().map(INSTANCE::amazonConvertDetailDTO).collect(java.util.stream.Collectors.toList()))"),
    })
    PlatformSoOutStockDTO amazonConvertDTO(String platformCode, List<PlatformAmazonFulfilledShipmentsDTO> sourceDetails);


    @Mappings({
            @Mapping(target = "platformDetailUniqueId", source = "shipmentItemId"),
            @Mapping(target = "platformCode", source = "amazonOrderId"),
            @Mapping(target = "platformOrderDetailId", source = "amazonOrderItemId"),
            @Mapping(target = "platformOrderCreateTime", expression = "java(java.time.OffsetDateTime.parse(sourceDetail.getPurchaseDate()))"),
            @Mapping(target = "platformPayTime", expression = "java(java.time.OffsetDateTime.parse(sourceDetail.getPaymentsDate()))"),
            @Mapping(target = "platformDeliveryTime", expression = "java(java.time.OffsetDateTime.parse(sourceDetail.getShipmentDate()))"),
            @Mapping(target = "qtyShipped", source = "quantityShipped"),
    })
    PlatformSoOutStockDetailDTO amazonConvertDetailDTO(PlatformAmazonFulfilledShipmentsDTO sourceDetail);
}
