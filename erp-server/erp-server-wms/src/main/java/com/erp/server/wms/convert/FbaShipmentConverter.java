package com.erp.server.wms.convert;

import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

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
            @Mapping(target = "sourceId", source = "mainId"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "demandType", constant = ""),
            @Mapping(target = "inventoryOrgId", constant = ""),
            @Mapping(target = "remark", constant = ""),
            @Mapping(target = "sourceType", constant = ""),
    })
    FirstMileDeliveryDTO.AddDTO fbaGenerateDeliverViewToDeliveryAdd(FbaShipmentDTO.GenerateDeliverView view);


    @Mappings({
            @Mapping(target = "platformSpuNo", source = "asin"),
            @Mapping(target = "platformSkuNo", source = "msku"),
            @Mapping(target = "declareQty", source = "declareQty"),
            @Mapping(target = "planQty", source = "deliveryQty"),
            @Mapping(target = "deliveryQty", source = "deliveryQty"),
            @Mapping(target = "netWeight", constant = "0"),
            @Mapping(target = "productSizeHeight", constant = "0"),
            @Mapping(target = "productSizeLength", constant = "0"),
            @Mapping(target = "productSizeWidth", constant = "0")
    })
    FirstMileDeliveryDetailDTO.AddDTO fbaGenerateDeliverViewToDeliveryDetailAdd(FbaShipmentDTO.GenerateDeliverView view);

    @Mappings({
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "outWarehouseId", source = "deliveryWarehouseId"),
            @Mapping(target = "inWarehouseId", source = "destWarehouseId"),
    })
    TransferOutDTO.AddDTO fbaDeliveryEntityToTransferOutAdd(FirstMileDeliveryEntity entity);

    @Mappings({
            @Mapping(target = "outWarehouseLocation", source = "warehouseLocation"),
            @Mapping(target = "qty", source = "deliveryQty"),
            @Mapping(target = "sourceDetailId", source = "id")
    })
    TransferOutDetailDTO.AddDTO fbaDeliveryDetailEntityToTransferOutDetailAdd(FirstMileDeliveryDetailEntity detailEntity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "shopId", source = "shopId"),
            @Mapping(target = "shopName", source = "shopName"),
            @Mapping(target = "countryId", source = "countryId"),
            @Mapping(target = "countryName", source = "countryName")
    })
    FirstMileDeliveryDTO.ViewDTO fbaShipmentEntityToFbaDeliveryViewDTO(FbaShipmentEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "planQty", source = "declareQty"),
            @Mapping(target = "deliveryQty", source = "declareQty"),
            @Mapping(target = "platformSkuNo", source = "msku"),
            @Mapping(target = "platformSpuNo", source = "asin")
    })
    FirstMileDeliveryDetailDTO.ViewDTO fbaShipmentDetailEntityToDeliveryDetailViewDTO(FbaShipmentDetailEntity detailEntity);


    @Mappings({
            @Mapping(target = "id", source = "oldEntity.id"),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", constant = "false"),
            @Mapping(target = "code", source = "oldEntity.code"),
            @Mapping(target = "fbaShipmentId", source = "oldEntity.fbaShipmentId"),
            @Mapping(target = "shipmentCreateTime", source = "oldEntity.shipmentCreateTime"),
            @Mapping(target = "deliveryStatus", source = "oldEntity.deliveryStatus"),
            // 新修改
            @Mapping(target = "name", source = "entity.name"),
            @Mapping(target = "shopId", source = "entity.shopId"),
            @Mapping(target = "shopName", source = "entity.shopName"),
            @Mapping(target = "countryId", source = "entity.countryId"),
            @Mapping(target = "countryName", source = "entity.countryName"),
            @Mapping(target = "fulfillmentCenter", source = "entity.fulfillmentCenter"),
            @Mapping(target = "platformShipmentStatus", source = "entity.platformShipmentStatus"),
            @Mapping(target = "shipmentReceiveTime", source = "entity.shipmentReceiveTime"),
            @Mapping(target = "labelType", source = "entity.labelType"),
            @Mapping(target = "packType", source = "entity.packType"),
            @Mapping(target = "deliveryFromAddress", source = "entity.deliveryFromAddress"),
            @Mapping(target = "deliveryToAddress", source = "entity.deliveryToAddress"),
            @Mapping(target = "isPackingDownload", source = "oldEntity.isPackingDownload"),
    })
    FbaShipmentEntity oldToNew(FbaShipmentEntity entity, FbaShipmentEntity oldEntity);

    @Mappings({
            @Mapping(target = "toWarehouseId", constant = ""),
            @Mapping(target = "toWarehouseName", constant = ""),
            @Mapping(target = "fromWarehouseId", constant = ""),
            @Mapping(target = "fromWarehouseName", constant = ""),
            @Mapping(target = "handleUserId", constant = ""),
            @Mapping(target = "detailList", ignore = true),
            @Mapping(target = "handleUserName", constant = ""),
            @Mapping(target = "requisitionWarehouseName", constant = "")
    })
    RequisitionApplicationDTO.AddDTO DeliveryPlanGRA(FbaShipmentDTO.GenerateRequisitionApplicationViewDTO dto);


    @Mappings({
            @Mapping(target = "approveQty", ignore = true),
            @Mapping(target = "pickingQty", ignore = true),
            @Mapping(target = "asin", source = "asin"),
            @Mapping(target = "fnSku", source = "fnSku"),
            @Mapping(target = "platformSku", source = "msku"),
    })
    RequisitionApplicationDetailDTO.AddDTO DeliveryPlanDetailGRA(FbaShipmentDTO.GenerateRequisitionApplicationViewDTO dto);
}
