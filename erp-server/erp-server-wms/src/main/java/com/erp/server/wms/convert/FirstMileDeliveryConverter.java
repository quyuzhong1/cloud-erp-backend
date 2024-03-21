package com.erp.server.wms.convert;


import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.PackingExcelDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.FirstMileDeliveryLogisticsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface FirstMileDeliveryConverter {
    FirstMileDeliveryConverter INSTANCE = Mappers.getMapper(FirstMileDeliveryConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "remark", ignore = true),
            @Mapping(target = "sourceId", source = "entity.id"),
            @Mapping(target = "sourceCode", source = "entity.code"),
            @Mapping(target = "toWarehouseId", source = "entity.destWarehouseId"),
            @Mapping(target = "toWarehouseName", source = "entity.destWarehouseName"),
            @Mapping(target = "logisticsMethod", source = "logisticsEntity.logisticsMethod"),
    })
    OverseasWarehouseInboundDTO.ViewDTO fmdToOverseasWarehouseInboundView(FirstMileDeliveryEntity entity, FirstMileDeliveryLogisticsEntity logisticsEntity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "isCombination", source = "isCombination"),
            @Mapping(target = "platformSkuNo", source = "stockSku"),
    })
    OverseasWarehouseInboundDetailDTO.ViewDTO fmdToOverseasWarehouseInboundDetailView(FirstMileDeliveryDetailEntity detailEntityList);
    List<OverseasWarehouseInboundDetailDTO.ViewDTO> fmdToOverseasWarehouseInboundDetailView(List<FirstMileDeliveryDetailEntity> detailEntityList);

    @Mappings({
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "sku"),
            @Mapping(target = "packQty", source = "singleBoxQuantity"),
    })
    WmsCartonDetailDTO.AddDTO importToPackingSku(PackingExcelDTO data);
    List<WmsCartonDetailDTO.AddDTO> importToPackingSku(List<PackingExcelDTO> data);
}
