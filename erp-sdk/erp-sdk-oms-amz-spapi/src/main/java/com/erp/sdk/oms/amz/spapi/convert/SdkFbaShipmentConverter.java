package com.erp.sdk.oms.amz.spapi.convert;

import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItem;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 亚马逊FBA货件转换器
 *
 * @author Jim
 * @since 2023-11-01
 **/
@Mapper(componentModel = "spring")
@Component
public interface SdkFbaShipmentConverter {
    SdkFbaShipmentConverter INSTANCE = Mappers.getMapper(SdkFbaShipmentConverter.class);

    @Mappings({
            @Mapping(source = "shipmentInfo.shipmentName", target = "name"),
            @Mapping(target = "countryId", source = "shipmentInfo.shipFromAddress.countryCode"),
//            @Mapping(target = "countryName", source = "shipmentInfo.countryName"),
            @Mapping(target = "fulfillmentCenter", source = "shipmentInfo.destinationFulfillmentCenterId"),
//            @Mapping(target = "shipmentStatus", source = "shipmentInfo.shipmentStatus.value"),
            @Mapping(target = "platformShipmentStatus", source = "shipmentInfo.shipmentStatus.value"),
            @Mapping(target = "shipmentCreateTime", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()))"),
            @Mapping(target = "labelType", constant = "shipmentInfo.labelType"),
            @Mapping(target = "packType", constant = "?"),
            @Mapping(target = "deliveryFromAddress", expression = "java(dto.combineDeliveryFromAddress())"),
            @Mapping(target = "fbaShipmentId", source = "shipmentInfo.shipmentId."),
            @Mapping(target = "downloadStatus", constant = "1"),
            @Mapping(target = "downloadTime"),
            @Mapping(target = "countryName", source = ""),
            @Mapping(target = "deliveryStatus", source = ""),
            @Mapping(target = "shipmentReceiveTime", source = ""),
            @Mapping(target = "receiveDTOList")
    })
    PlatformFbaShipmentDTO downloadDtoToSaveDto(PlatformAmazonFbaShipmentDTO dto);


    @Mappings({
            @Mapping(target = "MSku", source = "sellerSKU"),
            @Mapping(target = "declareQty", source = "quantityReceived"),
            @Mapping(target = "receiveQty", source = "quantityReceived"),
            @Mapping(target = "deliveryQty", source = "quantityShipped"),
            @Mapping(target = "fbaShipmentId", source = "shipmentId"),
            @Mapping(target = "receiveDate", expression = "java(java.time.LocalDateTime.now(java.time.ZoneId.systemDefault()))")
    })
    PlatformFbaShipmentReceiveDTO receiveDtoToSaveDto(InboundShipmentItem item);


    List<PlatformFbaShipmentReceiveDTO> receiveDtoToSaveDtoList(List<InboundShipmentItem> item);
}
