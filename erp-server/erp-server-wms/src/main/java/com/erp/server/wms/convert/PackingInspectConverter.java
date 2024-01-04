package com.erp.server.wms.convert;

import com.erp.model.wms.dto.PackingInspectionDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 包装验货映射
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface PackingInspectConverter {

    PackingInspectConverter INSTANCE = Mappers.getMapper(PackingInspectConverter.class);

    @Mappings({
            @Mapping(target = "id", source = "entity.id"),
            @Mapping(target = "status", source = "entity.isInspection"),
            @Mapping(target = "code", source = "entity.soCode"),
            @Mapping(target = "transportNo", source = "entity.transportNo"),
            @Mapping(target = "waitScanSkuList", source = "waitScanList"),
    })
    PackingInspectionDTO.ViewDTO convertViewDTO(SoB2cDeliveryEntity entity, List<SoB2cDeliveryDetailEntity> waitScanList);

    @Mappings({
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "warehouseLocation", source = "warehouseLocation"),
            @Mapping(target = "waitScanQty", source = "waitScanQty"),
            @Mapping(target = "scannedQty",  expression = "java(detailEntity.getDeliveryQty() - detailEntity.getWaitScanQty())"),
            @Mapping(target = "saleQty", source = "deliveryQty"),
    })
    PackingInspectionDTO.ViewDTO.ScanSkuInfo convertViewDTO(SoB2cDeliveryDetailEntity detailEntity);

}
