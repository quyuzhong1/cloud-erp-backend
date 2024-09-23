package com.erp.server.wms.convert;

import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;


@Mapper(uses = TypeConversionWorker.class)
@Component
public interface FbaShipmentPackingConverter {
    FbaShipmentPackingConverter INSTANCE = Mappers.getMapper(FbaShipmentPackingConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "mainId", source = "shipmentEntity.id"),
            @Mapping(target = "boxNo", source = "boxNo"),
            @Mapping(target = "msku", source = "packingDetailDTO.msku"),
            @Mapping(target = "fnSku", source = "packingDetailDTO.fnSku"),
            @Mapping(target = "asin", source = "packingDetailDTO.asin"),
            @Mapping(target = "qty", source = "packingDetailDTO.qty"),
            @Mapping(target = "skuNo", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\" :listingInfoWithSkuMappingDTO.checkAndGetProductSkuNo())"),
            @Mapping(target = "skuId", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\" :listingInfoWithSkuMappingDTO.checkAndGetProductSkuId())"),
    })
    FbaShipmentPackingEntity fbaShipmentPackingConvert(
            String boxNo,
            FbaShipmentPackingDTO.PackingDetailDTO packingDetailDTO,
            FbaShipmentEntity shipmentEntity,
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO);
}
