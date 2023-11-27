package com.erp.server.wms.convert;

import com.common.business.dto.PlatformInventoryDTO;
import com.common.business.dto.PlatformTransferWarehouseDTO;
import com.common.business.dto.PlatformWarehouseDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * 海外仓入库单
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface OverseasWarehouseInboundConverter {

    OverseasWarehouseInboundConverter INSTANCE = Mappers.getMapper(OverseasWarehouseInboundConverter.class);

    @Mappings({
//            @Mapping(target = "id",  ignore = true)
    })
    OverseasWarehouseInboundDTO.ViewDTO entityToViewDTO(OverseasWarehouseInboundEntity entity);


    @Mappings({
//            @Mapping(target = "id",  ignore = true)
    })
    OverseasWarehouseInboundDetailDTO.ViewDTO detailEntityToViewDTO(OverseasWarehouseInboundDetailEntity entity);

    @Mappings({
            @Mapping(target = "id",  source = "entity.id"),
            @Mapping(target = "receiveTime",  source = "entity.receiveTime"),
            @Mapping(target = "sourceId", source = "mainEntity.sourceId"),
            @Mapping(target = "deliveryWarehouseName", source = "mainEntity.deliveryWarehouseName"),
            @Mapping(target = "deliveryWarehouseId", source = "mainEntity.deliveryWarehouseId"),
            @Mapping(target = "transferWarehouseName", source = "mainEntity.transferWarehouseName"),
            @Mapping(target = "transferWarehouseId", source = "mainEntity.transferWarehouseId"),
            @Mapping(target = "toWarehouseName", source = "mainEntity.toWarehouseName"),
            @Mapping(target = "toWarehouseId", source = "mainEntity.toWarehouseId"),
    })
    OverseasWarehouseInboundDetailDTO.ViewListDTO detailEntityToViewListDTO(OverseasWarehouseInboundDetailEntity entity, OverseasWarehouseInboundEntity mainEntity);
}
