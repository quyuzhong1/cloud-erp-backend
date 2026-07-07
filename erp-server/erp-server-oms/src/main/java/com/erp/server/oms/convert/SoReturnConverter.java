package com.erp.server.oms.convert;

import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface SoReturnConverter {

    SoReturnConverter INSTANCE = Mappers.getMapper(SoReturnConverter.class);


    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "platformOrderId", source = "code"),
            @Mapping(target = "shopName", source = "customerName"),
            @Mapping(target = "status", constant = "4"),
            @Mapping(target = "salesRecordNumber", source = "code"),
            @Mapping(target = "platformName", source = "type"),
            @Mapping(target = "buyerName", source = "customerName"),
            @Mapping(target = "employeeId", source = "sellerId"),
            @Mapping(target = "employeeName", source = "sellerName"),
            @Mapping(target = "returnCreateTime", source = "createTime"),
//            @Mapping(target = "refundTime", source = "billDate"),
            @Mapping(target = "currencyCode", source = "currency"),
            @Mapping(target = "companyId", source = "salesOrgId"),
            @Mapping(target = "companyName", source = "salesOrgName"),
            @Mapping(target = "returnCode", source = "code"),
            @Mapping(target = "chargeId", source = "sellerId"),
            @Mapping(target = "chargeName", source = "sellerName"),
            @Mapping(target = "platformReturnCode", source = "code")
    })
    BiReturnOrderInfoEntity soReturnOrderToDmpReturn(SoReturnEntity soReturnEntity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "quantity", source = "returnQty"),
            @Mapping(target = "status", constant = "2"),
            @Mapping(target = "originalSkuNo", source = "skuNo")
    })
    BiReturnOrderItemEntity soReturnOrderToDmpReturnItem(SoReturnDetailEntity soReturnDetailEntity);

    SoReturnDTO.SoReturnListVO toListVO(SoReturnEntity soReturnEntity);
}
