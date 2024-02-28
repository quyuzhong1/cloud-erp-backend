package com.erp.server.wms.convert;

import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper()
@Component
public interface WarehouseReceiveConverter {
    WarehouseReceiveConverter INSTANCE = Mappers.getMapper(WarehouseReceiveConverter.class);

    @Mappings({
            @Mapping(target = "id", source = "detail.id"),
            @Mapping(target = "mainId", source = "main.id"),
            @Mapping(target = "stockInDate", expression = "java(java.time.LocalDate.now())"),
//            @Mapping(target = "stockInUserId", source = "fromWarehouseLocation"),
            @Mapping(target = "stockInQty", source = "detail.receiveQty"),
            @Mapping(target = "exceedQty", source = "detail.exceedQty"),
            @Mapping(target = "remark", source = "detail.remark"),
            @Mapping(target = "purchaseOrderDetailId", source = "detail.purchaseOrderDetailId"),
            @Mapping(target = "deliveryWarehouseId", source = "main.deliveryWarehouseId"),
    })
    WarehouseReceiveDTO.GenerateStockInDTO entityToGenerateStockConvert(WarehouseReceiveDetailEntity detail, WarehouseReceiveEntity main);

}
