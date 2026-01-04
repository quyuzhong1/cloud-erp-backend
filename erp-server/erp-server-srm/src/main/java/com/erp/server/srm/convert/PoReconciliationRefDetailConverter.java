package com.erp.server.srm.convert;

import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationRefDetailEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface PoReconciliationRefDetailConverter {
    PoReconciliationRefDetailConverter INSTANCE = Mappers.getMapper(PoReconciliationRefDetailConverter.class);


    @Mapping(target = "poReconciliationDetailId", source = "id")
    @Mapping(target = "supplierId", source = "supplierId")
    @Mapping(target = "supplierName", source = "supplierName")
    @Mapping(target = "sourceDetailId", source = "sourceDetailId")
    @Mapping(target = "sourceId", source = "sourceId")
    @Mapping(target = "sourceCode", source = "sourceCode")
    @Mapping(target = "sourceType", source = "sourceType")
    @Mapping(target = "poCode", source = "poCode")
    @Mapping(target = "poId", source = "poId")
    @Mapping(target = "poDetailId", source = "poDetailId")
    @Mapping(target = "date", source = "date")
    @Mapping(target = "skuId", source = "skuId")
    @Mapping(target = "skuNo", source = "skuNo")
    @Mapping(target = "settleOrgId", source = "settleOrgId")
    @Mapping(target = "settleOrgName", source = "settleOrgName")
    @Mapping(target = "settleDict", source = "settleDict")
    @Mapping(target = "paymentCondition", source = "paymentCondition")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "exchangeRate", source = "exchangeRate")
    @Mapping(target = "returnSourceType", source = "returnSourceType")
    @Mapping(target = "deliveryId", source = "deliveryId")
    @Mapping(target = "deliveryDetailId", source = "deliveryDetailId")
    @Mapping(target = "deliveryCode", source = "deliveryCode")
    @Mapping(target = "remark", source = "remark")
    PoReconciliationRefDetailEntity poReconciliationDetailToPayableDetailEntity(PoReconciliationDetailEntity detailEntity);
}
