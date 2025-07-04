package com.erp.server.wms.convert;


import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
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
            @Mapping(target = "platformFnSku", source = "platformFnSku"),
            @Mapping(target = "platformSkuName", source = "platformSkuName"),
    })
    RequisitionApplicationDetailEntity detailConvert(RequisitionApplicationDetailDTO.AddDTO detailList);
    List<RequisitionApplicationDetailEntity> detailConvert(List<RequisitionApplicationDetailDTO.AddDTO> detailList);

    @Mappings({
            @Mapping(target = "platformSku", source = "platformSku"),
            @Mapping(target = "platformSpu", source = "asin"),
            @Mapping(target = "platformFnSku", source = "platformFnSku"),
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

    @Mappings({
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "sourceType", expression = "java(com.common.business.enums.SourceTypeEnum.DELIVERY_PLAN.getCode())"),
            @Mapping(target = "status", expression = "java(com.erp.model.wms.enums.RequisitionApplicationStatusEnum.HANDLE.getCode())"),
            @Mapping(target = "invalidStatus", ignore = true),
            @Mapping(target = "invalidRemark", ignore = true),
            @Mapping(target = "invalidTime", ignore = true),
            @Mapping(target = "type", source = "type"),
            @Mapping(target = "channelId", source = "shopId"),
            @Mapping(target = "channelName", source = "shopName"),
            @Mapping(target = "requisitionWarehouseId", source = "fromWarehouseId"),
            @Mapping(target = "requisitionWarehouseName", source = "fromWarehouseName"),
            @Mapping(target = "handleUserId", source = "approveUserId"),
            @Mapping(target = "handleUserName", source = "approveUserName"),
            @Mapping(target = "handleTime", source = "approveTime"),
            @Mapping(target = "remark", source = "remark"),
            @Mapping(target = "fbaShipmentCode", ignore = true),
            @Mapping(target = "pickPushDownStatus", expression = "java(com.erp.model.wms.enums.BillPushDownStatusEnum.WAIT.getCode())"),
            @Mapping(target = "deliveryPushDownStatus", expression = "java(com.erp.model.wms.enums.BillPushDownStatusEnum.WAIT.getCode())"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "isUnlockInventory", ignore = true)
    })
    RequisitionApplicationEntity wmsDeliveryPlanToRequisitionApplication(WmsDeliveryPlanEntity planEntity);
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "approveQty",  source = "qty"),
            @Mapping(target = "bomVersion", ignore = true),
            @Mapping(target = "changeBeforeQty", ignore = true),
            @Mapping(target = "deliveryQty", source = "qty"),
            @Mapping(target = "fromVirtualWarehouseId", ignore = true),
            @Mapping(target = "fromVirtualWarehouseName", ignore = true),
            @Mapping(target = "fromWarehouseId", ignore = true),
            @Mapping(target = "fromWarehouseName", ignore = true),
            @Mapping(target = "pickingQty", source = "qty"),
            @Mapping(target = "requisitionQty", source = "qty"),
            @Mapping(target = "requisitionWarehouseLocation", ignore = true),
            @Mapping(target = "sourceDetailId", source = "detailEntity.id"),
            @Mapping(target = "toWarehouseId", ignore = true),
            @Mapping(target = "toWarehouseName", ignore = true),
            @Mapping(target = "virtualFrozenQty", ignore = true)
    })
    RequisitionApplicationDetailEntity wmsDeliveryPlanDetailToRequisitionApplicationDetail(WmsDeliveryPlanDetailEntity detailEntity);
    List<RequisitionApplicationDetailEntity> wmsDeliveryPlanDetailToRequisitionApplicationDetail(List<WmsDeliveryPlanDetailEntity> planDetailEntityList);
}
