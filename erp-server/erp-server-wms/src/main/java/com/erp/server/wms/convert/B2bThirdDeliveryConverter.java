package com.erp.server.wms.convert;

import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.codehaus.janino.Java;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper(uses = TypeConversionWorker.class)
@Component
public interface B2bThirdDeliveryConverter {
    B2bThirdDeliveryConverter INSTANCE = Mappers.getMapper(B2bThirdDeliveryConverter.class);

    @Mapping(target = "warehouseOperationTypeName", expression = "java(com.erp.model.wms.enums.WarehouseOperationTypeEnum.getName(entity.getWarehouseOperationType()))")
    @Mapping(target = "statusName", expression = "java(com.erp.model.wms.enums.ThirdDeliveryStatusEnum.getName(entity.getWarehouseOperationType()))")
    @Mapping(target = "deliveryMethodName", expression = "java(com.erp.model.wms.enums.DeliveryMethodEnum.TRUCK_SELF.getName())")
    @Mapping(target = "detailList", source = "detailEntityList")
    B2bThirdDeliveryDTO.ViewDTO toB2bThirdDeliveryViewDTO(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList);
    B2bThirdDeliveryDetailDTO.ViewDTO toB2bThirdDeliveryDetailViewDTO(B2bThirdDeliveryDetailEntity deliveryDetail);
}
