package com.erp.server.srm.convert;

import com.erp.model.scm.dto.PurchaseStatisticsDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.srm.dto.HomePageDTO;
import com.erp.model.srm.entity.PurchaseOrderDetailEntity;
import com.erp.model.wms.dto.PurchaseReturnStatisticsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper()
public interface PurchaseOrderConverter {
    PurchaseOrderConverter INSTANCE = Mappers.getMapper(PurchaseOrderConverter.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "purchaseOrderDetailId", source = "purchaseOrderDetail.id")
    @Mapping(target = "skuId", source = "purchaseOrderDetail.skuId")
    @Mapping(target = "skuNo", source = "purchaseOrderDetail.skuNo")
    @Mapping(target = "productName", source = "purchaseOrderDetail.productName")
    @Mapping(target = "purchaseQty", source = "purchaseOrderDetail.purchaseQty")
    @Mapping(target = "purchaseAmount", source = "purchaseOrderDetail.purchaseAmount")
    @Mapping(target = "planDeliveryDate", source = "purchaseOrderDetail.planDeliveryDate")
    @Mapping(target = "isGift", source = "purchaseOrderDetail.isGift")
    @Mapping(target = "remark", source = "purchaseOrderDetail.remark")
    @Mapping(target = "taxRate", source = "purchaseOrderDetail.taxRate")
    @Mapping(target = "isUrgent", source = "purchaseOrderDetail.isUrgent")
    @Mapping(target = "currencySymbol", source = "purchaseOrderDetail.currencySymbol")
    @Mapping(target = "sourceDetailId", source = "purchaseOrderDetail.sourceDetailId")
    @Mapping(target = "executionStatus", source = "purchaseOrderDetail.executionStatus")
    @Mapping(target = "confirmType", source = "purchaseOrderDetail.confirmType")
    @Mapping(target = "code", source = "purchaseOrder.code")
    @Mapping(target = "purchaseUserId", source = "purchaseOrder.purchaseUserId")
    @Mapping(target = "purchaseUserName", source = "purchaseOrder.purchaseUserName")
    @Mapping(target = "purchaseOrgId", source = "purchaseOrder.purchaseOrgId")
    @Mapping(target = "purchaseOrgName", source = "purchaseOrder.purchaseOrgName")
    @Mapping(target = "deliveryWarehouseId", source = "purchaseOrder.deliveryWarehouseId")
    @Mapping(target = "deliveryWarehouseName", source = "purchaseOrder.deliveryWarehouseName")
    @Mapping(target = "type", source = "purchaseOrder.type")
    @Mapping(target = "supplierId", source = "orderSupplierEntity.supplierId")
    PurchaseOrderDetailEntity scmPurchaseOrderToSrmPurchaseOrderDetail(PurchaseOrderEntity purchaseOrder, com.erp.model.scm.entity.PurchaseOrderDetailEntity purchaseOrderDetail, PurchaseOrderSupplierEntity orderSupplierEntity );
}
