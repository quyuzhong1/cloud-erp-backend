package com.erp.server.wms.convert;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * FBA货件实体映射工具
 * @Author Luo_WG
 * @Date 2023/10/31 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface FbaShipmentConverter {
    FbaShipmentConverter INSTANCE = Mappers.getMapper(FbaShipmentConverter.class);

    @Mapping(target = "detailList", ignore = true)
    FbaShipmentDTO.ViewDTO fbaShipmentToViewDTO(FbaShipmentEntity shipmentEntity);

    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "deliveryQty", ignore = true)
    FbaShipmentDetailDTO.ViewDTO fbaShipmentDetailToViewDTO(FbaShipmentDetailEntity detailEntity);

    @Mapping(target = "receiveTime", source = "receiveDate")
    FbaShipmentDTO.ReceiveRecordView fbaShipmentReceiveEntityToView(FbaShipmentReceiveEntity entities);

    @Mapping(target = "shipmentStatus", source = "platformShipmentStatus")
    FbaShipmentDTO.ShipmentStatusRecordView fbaShipmentStatusEntityToView(FbaShipmentStatusEntity entities);

    @Mappings({
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "attachNameList", defaultValue = ""),
            @Mapping(target = "attachUrlList", defaultValue = ""),
            @Mapping(target = "demandType", defaultValue = ""),
            @Mapping(target = "detailList", defaultValue = ""),
            @Mapping(target = "inventoryOrgId", defaultValue = ""),
            @Mapping(target = "remark", defaultValue = ""),
            @Mapping(target = "sourceType", defaultValue = ""),
            @Mapping(target = "shopName", source = "shopName", defaultValue = ""),
            @Mapping(target = "countryName", source = "countryName", defaultValue = "")
    })
    FbaDeliveryDTO.AddDTO fbaGenerateDeliverViewToDeliveryAdd(FbaShipmentDTO.GenerateDeliverView view);


    @Mappings({
            @Mapping(target = "declareQty", source = "declareQty"),
            @Mapping(target = "planQty", source = "deliveryQty"),
            @Mapping(target = "deliveryQty", source = "deliveryQty"),
            @Mapping(target = "netWeight", defaultValue = "0"),
            @Mapping(target = "productSizeHeight", defaultValue = "0"),
            @Mapping(target = "productSizeLength", defaultValue = "0"),
            @Mapping(target = "productSizeWidth", defaultValue = "0"),
            @Mapping(target = "stockSku", defaultValue = "")
    })
    FbaDeliveryDetailDTO.AddDTO fbaGenerateDeliverViewToDeliveryDetailAdd(FbaShipmentDTO.GenerateDeliverView view);

    @Mappings({
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "outWarehouseId", source = "deliveryWarehouseId"),
            @Mapping(target = "inWarehouseId", source = "destWarehouseId"),
    })
    TransferOutDTO.AddDTO fbaDeliveryEntityToTransferOutAdd(FbaDeliveryEntity entity);

    @Mappings({
            @Mapping(target = "outWarehouseLocation", source = "warehouseLocation"),
            @Mapping(target = "qty", source = "deliveryQty"),
            @Mapping(target = "sourceDetailId", source = "id")
    })
    TransferOutDetailDTO.AddDTO fbaDeliveryDetailEntityToTransferOutDetailAdd(FbaDeliveryDetailEntity detailEntity);


    @Mappings({
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "shopId", source = "shopId"),
            @Mapping(target = "shopName", source = "shopName"),
            @Mapping(target = "countryId", source = "countryId"),
            @Mapping(target = "countryName", source = "countryName")
    })
    FbaDeliveryDTO.ViewDTO fbaShipmentEntityToFbaDeliveryViewDTO(FbaShipmentEntity entity);

    @Mappings({
            @Mapping(target = "planQty", source = "declareQty"),
            @Mapping(target = "deliveryQty", source = "declareQty"),
    })
    FbaDeliveryDetailDTO.ViewDTO fbaShipmentDetailEntityToDeliveryDetailViewDTO(FbaShipmentDetailEntity detailEntity);
}
