package com.erp.server.srm.convert;

import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper()
public interface DeliveryOrderConverter {
    DeliveryOrderConverter INSTANCE = Mappers.getMapper(DeliveryOrderConverter.class);

    @Mapping(target = "detailId", source = "id")
    @Mapping(target = "purchaseQty", source = "orderQty")
    DeliveryOrderDetailDTO.ViewDTO detailViewConvert(DeliveryOrderDetailEntity detail);
    List<DeliveryOrderDetailDTO.ViewDTO> detailViewConvert(List<DeliveryOrderDetailEntity> details);

    @Mapping(target = "detailList", source = "detailList")
    @Mapping(target = "receiptStatusName", expression = "java(com.common.core.constant.EnumMessage.getNameByCode(com.erp.model.srm.enums.DeliveryOrderEnum.ReceiptStatusEnum.class,entity.getReceiptStatus()))")
    DeliveryOrderDTO.ViewDTO viewConvert(DeliveryOrderEntity entity,List<DeliveryOrderDetailEntity> detailList);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "supplierId", source = "addDeliveryDTO.supplierId")
    @Mapping(target = "sourceId", source = "purchaseOrderEntity.id")
    @Mapping(target = "sourceCode", source = "purchaseOrderEntity.code")
    @Mapping(target = "customerName", source = "purchaseOrderEntity.purchaseOrgName")
    @Mapping(target = "contactId", source = "purchaseOrderEntity.purchaseUserId")
    @Mapping(target = "contactName", source = "purchaseOrderEntity.purchaseUserName")
    @Mapping(target = "toWarehouseId", source = "purchaseOrderEntity.deliveryWarehouseId")
    @Mapping(target = "toWarehouseName", source = "purchaseOrderEntity.deliveryWarehouseName")
    @Mapping(target = "sourceType", constant = "purchase")
    @Mapping(target = "planDeliveryDate", source = "addDeliveryDTO.expectDeliveryDate")
    DeliveryOrderEntity purchaseOrderToDeliveryOrder(PurchaseOrderEntity purchaseOrderEntity, DeliveryOrderDTO.AddDeliveryDTO addDeliveryDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainId", source = "mainId")
    @Mapping(target = "sourceDetailId", source = "detailEntity.id")
    @Mapping(target = "skuId", source = "detailEntity.skuId")
    @Mapping(target = "skuNo", source = "detailEntity.skuNo")
    @Mapping(target = "productName", source = "detailEntity.productName")
    @Mapping(target = "deliveryQty", source = "addDeliveryDTO.planDeliveryQty")
    @Mapping(target = "isUrgent", source = "detailEntity.isUrgent")
    @Mapping(target = "orderQty", source = "detailEntity.purchaseQty")
    @Mapping(target = "planDeliveryDate", source = "addDeliveryDTO.expectDeliveryDate")
    DeliveryOrderDetailEntity purchaseOrderDetailToDeliveryOrderDetail(String mainId, DeliveryOrderDTO.AddDeliveryDTO addDeliveryDTO, PurchaseOrderDetailEntity detailEntity);
}
