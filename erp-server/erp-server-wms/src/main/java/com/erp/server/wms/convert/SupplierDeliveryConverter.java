package com.erp.server.wms.convert;


import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface SupplierDeliveryConverter {
    SupplierDeliveryConverter INSTANCE = Mappers.getMapper(SupplierDeliveryConverter.class);

    @Mappings({
            @Mapping(target = "sourceId", source = "deliveryOrderEntity.id"),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.wms.enums.PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode())"),
            @Mapping(target = "purchaseOrderId", source = "deliveryOrderEntity.sourceId"),
            @Mapping(target = "purchaseOrderCode", source = "deliveryOrderEntity.sourceCode"),
            @Mapping(target = "receiveUserId", source = "deliveryOrderEntity.receiveUserId"),
            @Mapping(target = "billDate", expression = "java(java.time.LocalDate.now())"),
            @Mapping(target = "deliveryWarehouseId", source = "deliveryOrderEntity.toWarehouseId"),
            @Mapping(target = "warehouseReceiveDetailList", source = "detailEntityGroupList"),
    })
    WarehouseReceiveDTO.AddDTO deliveryToReceiveConvert(DeliveryOrderEntity deliveryOrderEntity, List<DeliveryOrderDetailEntity> detailEntityGroupList);

    @Mappings({
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "receiveQty", source = "receiveQty"),
            @Mapping(target = "exceedQty", source = "giftReceiveQty"),
            @Mapping(target = "purchaseOrderDetailId", source = "sourceDetailId"),
            @Mapping(target = "sourceDetailId", source = "id"),
    })
    WarehouseReceiveDetailDTO.AddDTO deliveryDetailToReceiveConvert(DeliveryOrderDetailEntity detailEntity);
    List<WarehouseReceiveDetailDTO.AddDTO> deliveryDetailToReceiveConvert(List<DeliveryOrderDetailEntity> detailEntityGroupList);
}
