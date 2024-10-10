package com.erp.server.wms.convert;


import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 要货申请
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface RequisitionApplicationConverter {

    RequisitionApplicationConverter INSTANCE = Mappers.getMapper(RequisitionApplicationConverter.class);

    RequisitionApplicationDetailDTO.ViewDTO radEntityToRadDto(RequisitionApplicationDetailEntity entity);

    @Mappings({
            @Mapping(target = "qty", source = "approveQty"),
            @Mapping(target = "outWarehouseId", source = "fromWarehouseId"),
            @Mapping(target = "outWarehouseLocation", source = "fromWarehouseLocation"),
            @Mapping(target = "inWarehouseId", source = "toWarehouseId"),
            @Mapping(target = "inWarehouseLocation", source = "toWarehouseLocation"),
    })
    TransferInfoDetailDTO.AddDTO radHandleListToTransferInfoDetail(RequisitionApplicationDTO.HandleListDTO handleListDTO);

    @Mappings({
            @Mapping(target = "qty", source = "pickingQty"),
            @Mapping(target = "outWarehouseId", source = "toWarehouseId"),
            @Mapping(target = "inWarehouseId", source = "requisitionWarehouseId"),
            @Mapping(target = "inWarehouseLocation", source = "requisitionWarehouseLocation"),
    })
    TransferInfoDetailDTO.AddDTO radFinishListToTransferInfoDetail(RequisitionApplicationDTO.FinishListDTO finishListDTO);

    @Mappings({
            @Mapping(target = "platformSku", source = "platformSku"),
            @Mapping(target = "platformSpu", source = "asin"),
            @Mapping(target = "platformFnSku", source = "fnSku"),
            @Mapping(target = "platformSkuName", source = "platformSkuName"),
    })
    RequisitionApplicationDetailEntity detailConvert(RequisitionApplicationDetailDTO.AddDTO detailList);
    List<RequisitionApplicationDetailEntity> detailConvert(List<RequisitionApplicationDetailDTO.AddDTO> detailList);

    @Mappings({
            @Mapping(target = "platformSku", source = "platformSku"),
            @Mapping(target = "platformSpu", source = "asin"),
            @Mapping(target = "platformFnSku", source = "fnSku"),
            @Mapping(target = "platformSkuName", source = "platformSkuName"),
    })
    RequisitionApplicationDetailEntity detailUpdateConvert(RequisitionApplicationDetailDTO.UpdateDTO detailList);
    List<RequisitionApplicationDetailEntity> detailUpdateConvert(List<RequisitionApplicationDetailDTO.UpdateDTO> detailList);

    @Mappings({
            @Mapping(target = "destWarehouseId", source = "toWarehouseId"),
            @Mapping(target = "destWarehouseName", source = "toWarehouseName"),
            @Mapping(target = "countryId", source = "country")
    })
    FirstMileDeliveryDTO.AddDTO generateDeliverFDD(RequisitionApplicationDTO.GenerateDeliverViewDTO dto);

    @Mappings({
            @Mapping(target = "platformSkuNo", source = "platformSku")
    })
    FirstMileDeliveryDetailDTO.AddDTO generateDeliverDetailFDD(RequisitionApplicationDTO.GenerateDeliverViewDTO dto);

    @Mappings({
            @Mapping(target = "sourceId", source = "entity.id"),
            @Mapping(target = "sourceType", expression = "java(com.common.business.enums.SourceTypeEnum.REQUISITION_APPLICATION.getCode())"),
            @Mapping(target = "sourceCode", source = "entity.code"),
            @Mapping(target = "demandType", expression = "java(com.erp.model.wms.enums.FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode())"),
            @Mapping(target = "shopId", source = "shopInfo.id"),
            @Mapping(target = "shopName", source = "shopInfo.name"),
            @Mapping(target = "countryId", source = "fbaShipmentEntity.countryId"),
            @Mapping(target = "countryName", source = "fbaShipmentEntity.countryName"),
            @Mapping(target = "deliveryWarehouseId", source = "entity.requisitionWarehouseId"),
            @Mapping(target = "deliveryWarehouseName", source = "entity.requisitionWarehouseName"),
            @Mapping(target = "destWarehouseId", source = "shopInfo.warehouseId"),
            @Mapping(target = "destWarehouseName", source = "shopInfo.warehouseName"),
            @Mapping(target = "remark", ignore = true),
            @Mapping(target = "fulfillmentCenter", source = "shopInfo.warehouseName"),
            @Mapping(target = "inventoryOrgId", ignore = true),
    })
    FirstMileDeliveryDTO.AddDTO generateFbaDeliverFDD(FbaShipmentEntity fbaShipmentEntity, RequisitionApplicationEntity entity, ShopInfoEntity shopInfo);


    @Mappings({
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "platformSpuNo", source = "fbaShipmentDetailEntity.asin"),
            @Mapping(target = "platformSkuNo", source = "fbaShipmentDetailEntity.msku"),
            @Mapping(target = "fnSku", source = "fbaShipmentDetailEntity.fnSku"),
            @Mapping(target = "skuId", source = "fbaShipmentDetailEntity.skuId"),
            @Mapping(target = "skuNo", source = "fbaShipmentDetailEntity.skuNo"),
            @Mapping(target = "declareQty", source = "fbaShipmentDetailEntity.declareQty"),
            @Mapping(target = "planQty", source = "fbaShipmentDetailEntity.declareQty"),
            @Mapping(target = "deliveryQty", ignore = true),
            @Mapping(target = "isCombination", source = "fbaShipmentDetailEntity.isCombination"),
            @Mapping(target = "netWeight", source = "skuVO.netWeight"),
            @Mapping(target = "productSizeLength", source = "skuVO.productLength"),
            @Mapping(target = "productSizeWidth", source = "skuVO.productWidth"),
            @Mapping(target = "productSizeHeight", source = "skuVO.productHeight"),
            @Mapping(target = "warehouseLocation", ignore = true),
            @Mapping(target = "sourceDetailId", source = "fbaShipmentDetailEntity.id"),
            @Mapping(target = "fbaShipmentCode", ignore = true),
    })
    FirstMileDeliveryDetailDTO.AddDTO generateFbaDeliverDetailFDD(FbaShipmentDetailEntity fbaShipmentDetailEntity, SkuVO skuVO);
}
