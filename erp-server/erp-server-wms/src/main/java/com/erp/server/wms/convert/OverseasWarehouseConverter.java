package com.erp.server.wms.convert;

import com.common.business.dto.PlatformInventoryDTO;
import com.common.business.dto.PlatformTransferWarehouseDTO;
import com.common.business.dto.PlatformWarehouseDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.entity.OverseasTransferWarehouseEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 海外仓
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface OverseasWarehouseConverter {

    OverseasWarehouseConverter INSTANCE = Mappers.getMapper(OverseasWarehouseConverter.class);

    @Mappings({
            @Mapping(target = "mainId", source = "providerErpId"),
            @Mapping(target = "platformWarehouseCode", source = "warehouseCode"),
            @Mapping(target = "platformWarehouseName", source = "warehouseName"),
            @Mapping(target = "country", source = "countryCode"),
            @Mapping(target = "countryName", source = "countryName"),
            @Mapping(target = "warehouseCode", ignore = true),
            @Mapping(target = "warehouseId", ignore = true),
            @Mapping(target = "warehouseName",  ignore = true),
            @Mapping(target = "platformWarehouseStatus", source = "platformWarehouseStatus"),
            @Mapping(target = "platformWarehouseType", source = "platformWarehouseType")
    })
    OverseasProviderWarehouseEntity warehouseDb(PlatformWarehouseDTO dto);

    @Mappings({
            @Mapping(target = "dictPlatform", source = "provider"),
            @Mapping(target = "platformWarehouseCode", source = "transferWarehouseCode"),
            @Mapping(target = "name", source = "transferWarehouseName"),
            @Mapping(target = "platformToWarehouseCode", source = "destinationWarehouseCode"),
            @Mapping(target = "platformToWarehouseName", source = "destinationWarehouseName"),
            @Mapping(target = "logisticsProductCode", source = "logisticsChannelCode"),
            @Mapping(target = "logisticsProductName", source = "logisticsChannelName"),
            @Mapping(target = "platformWarehouseStatus", source = "platformWarehouseStatus"),
            @Mapping(target = "country", source = "countryCode"),
            @Mapping(target = "overseasProviderId", source = "providerErpId")
    })
    OverseasTransferWarehouseEntity transferDtoConvert(PlatformTransferWarehouseDTO dto);

    @Mappings({
            @Mapping(target = "warehouseCode", source = "platformWarehouseCode"),
            @Mapping(target = "dictPlatform", source = "provider"),
            @Mapping(target = "name", source = "platformWarehouseName"),
            @Mapping(target = "platformSku", source = "productSku"),
            @Mapping(target = "deliverOnwayQty", source = "transferOnway"),
            @Mapping(target = "pendingQty", source = "pending"),
            @Mapping(target = "sellableQty", source = "sellable"),
            @Mapping(target = "unsellableQty", source = "unsellable"),
            @Mapping(target = "reservedQty", source = "reserved"),
            @Mapping(target = "onwayQty", source = "onway"),
            @Mapping(target = "lackQty", source = "piNoStock"),
            @Mapping(target = "frozenQty", source = "piFreeze"),
            @Mapping(target = "shippedQty", source = "shipped"),
            @Mapping(target = "downloadTime", source = "downloadTime"),
            @Mapping(target = "saleReturnInTransitQty", source = "saleReturnInTransitQty"),
            @Mapping(target = "overseasProviderId", source = "providerErpId"),
    })
    OverseasInventoryEntity inventoryDtoToDb(PlatformInventoryDTO dto);

    OverseasProviderWarehouseDTO.ShippedViewDTO inventoryToShipmentDTO(OverseasInventoryEntity overseasInventory);
    List<OverseasProviderWarehouseDTO.ShippedViewDTO> inventoryToShipmentDTO(List<OverseasInventoryEntity> overseasInventoryEntities);
}
