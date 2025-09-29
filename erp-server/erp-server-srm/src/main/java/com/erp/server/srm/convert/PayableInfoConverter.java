package com.erp.server.srm.convert;

import com.erp.model.srm.dto.PayableDetailDTO;
import com.erp.model.srm.dto.PayableInfoDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface PayableInfoConverter {
    PayableInfoConverter INSTANCE = Mappers.getMapper(PayableInfoConverter.class);

    @Mapping(target = "supplierId", source = "supplierId")
    @Mapping(target = "orgId", source = "settleOrgId")
    @Mapping(target = "sourceType", constant = "payableInfo")
    @Mapping(target = "sourceId", source = "id")
    @Mapping(target = "sourceCode", source = "code")
    @Mapping(target = "remark", source = "remark")
    PayableInfoDTO.AddDTO poReconciliationToPayableEntity(PoReconciliationEntity poReconciliation);


    @Mapping(target = "skuId", source = "skuId")
    @Mapping(target = "qty", source = "qty")
    @Mapping(target = "taxIncludedPrice", source = "taxPrice")
    @Mapping(target = "taxRate", source = "taxRate")
    @Mapping(target = "discountRate", source = "discountRate")
    @Mapping(target = "prepayAmount", source = "prepayAmount")
    @Mapping(target = "discountTaxAmount", source = "discountTaxAmount")
    @Mapping(target = "taxIncludedTotal", source = "taxAmount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "exchangeRate", source = "exchangeRate")
    @Mapping(target = "sourceDetailId", source = "id")
    @Mapping(target = "poDetailId", source = "poDetailId")
    @Mapping(target = "poId", source = "poId")
    @Mapping(target = "businessId", source = "sourceId")
    @Mapping(target = "businessCode", source = "sourceCode")
    @Mapping(target = "businessType", source = "sourceType")
    @Mapping(target = "businessDetailId", source = "sourceDetailId")
    @Mapping(target = "remark", source = "remark")
    PayableDetailDTO.AddDTO poReconciliationDetailToPayableDetailEntity(PoReconciliationDetailEntity detailEntity);
}
