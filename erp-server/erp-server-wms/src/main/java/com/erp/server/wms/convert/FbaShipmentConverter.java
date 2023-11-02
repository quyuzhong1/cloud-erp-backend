package com.erp.server.wms.convert;

import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.dto.FbaDeliveryDetailDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.FbaShipmentDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
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

    FbaShipmentDTO.ViewDTO fbaShipmentToViewDTO(FbaShipmentEntity shipmentEntity);

    FbaShipmentDetailDTO.ViewDTO fbaShipmentDetailToViewDTO(FbaShipmentDetailEntity detailEntity);

    @Mapping(target = "receiveTime", source = "receiveDate")
    @Mapping(target = "receiveQty", source = "receiveQty")
    FbaShipmentDTO.ReceiveRecordView fbaShipmentReceiveEntityToView(FbaShipmentReceiveEntity entities);

    @Mapping(target = "shipmentStatus", source = "platformShipmentStatus")
    @Mapping(target = "updateTime", source = "updateTime")
    FbaShipmentDTO.ShipmentStatusRecordView fbaShipmentStatusEntityToView(FbaShipmentStatusEntity entities);

    @Mappings({
        @Mapping(target = "sourceId", source = "view.id"),
        @Mapping(target = "sourceCode", source = "view.code"),
        @Mapping(target = "shopId", source = "view.shopId"),
        @Mapping(target = "shopName", source = "view.shopName"),
        @Mapping(target = "countryId", source = "view.countryId"),
        @Mapping(target = "countryName", source = "view.countryName"),
        @Mapping(target = "deliveryWarehouseId", source = "view.deliveryWarehouseId"),
        @Mapping(target = "destWarehouseId", source = "view.destWarehouseId"),
    })
    FbaDeliveryDTO.AddDTO fbaGenerateDeliverViewToDeliveryAdd(FbaShipmentDTO.GenerateDeliverView view, List<WarehouseEntity> warehouseEntities, List<BaseIdDTO.CodeDTO> accountingCompanyList);


    @Mappings({
        @Mapping(target = "mainId", source = "mainId"),
/*        @Mapping(target = "asin", source = "asin"),
        @Mapping(target = "mSku", source = "mSku"),
        @Mapping(target = "fnSku", source = "fnSku"),
        @Mapping(target = "skuNo", source = "skuNo"),
        @Mapping(target = "productName", source = ""),
        @Mapping(target = "stockSku", source = ""),
        @Mapping(target = "declareQty", source = "declareQty"),
        @Mapping(target = "planQty", source = "planQty"),
        @Mapping(target = "deliveryQty", source = "deliveryQty"),
        @Mapping(target = "isCombo", source = "isCombo"),
        @Mapping(target = "netWeight", source = ""),
        @Mapping(target = "productSizeLength", source = ""),
        @Mapping(target = "productSizeWidth", source = ""),
        @Mapping(target = "productSizeHeight", source = "")*/
    })
    FbaDeliveryDetailDTO.AddDTO fbaGenerateDeliverViewToDeliveryDetailAdd(FbaShipmentDTO.GenerateDeliverView view);
}
