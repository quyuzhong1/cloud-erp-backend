package com.erp.server.dmp.convert;

import com.common.business.dto.PlatformAwdShipmentDTO;
import com.common.business.dto.PlatformAwdShipmentReceiveDTO;
import com.erp.model.dmp.entity.DmpAwdShipmentDetailEntity;
import com.erp.model.dmp.entity.DmpAwdShipmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zdy
 * @ClassName DmpAwdShipmentConverter
 * @description: TODO
 * @date 2024年10月08日
 * @version: 1.0
 */
@Mapper
@Component
public interface DmpAwdShipmentConverter {
    DmpAwdShipmentConverter INSTANCE = Mappers.getMapper(DmpAwdShipmentConverter.class);

    @Mapping(target = "uniqueId", source = "dmpMainEntity.fbaShipmentId")
    @Mapping(target = "shopName", ignore = true)
    @Mapping(target = "shopId", source = "dmpMainEntity.nextLevelId")
    @Mapping(target = "referenceId", source = "dmpMainEntity.referenceId")
    @Mapping(target = "shipmentReceiveTime", ignore = true)
    @Mapping(target = "shipmentCreateTime", source = "dmpMainEntity.platformCreateTime")
    @Mapping(target = "platform", expression = "java(com.common.business.enums.PlatformDictEnum.AMAZON.getCode())")
    @Mapping(target = "planCode", source = "dmpMainEntity.platformOrderId")
    @Mapping(target = "orderType", ignore = true)
    @Mapping(target = "dmpSyncTaskId", source = "dmpMainEntity.inputTaskId")
    @Mapping(target = "detailList", source = "dmpDetailEntityList")
    @Mapping(target = "deliveryStatus", source = "dmpMainEntity.platformShipmentStatus")
    @Mapping(target = "deliveryId", ignore = true)
    @Mapping(target = "deliveryCode", ignore = true)
    @Mapping(target = "countryName", ignore = true)
    PlatformAwdShipmentDTO convertToPlatformAwdShipmentDTO(DmpAwdShipmentEntity dmpMainEntity, List<DmpAwdShipmentDetailEntity> dmpDetailEntityList);

    @Mapping(target = "sellerSku", ignore = true)
    @Mapping(target = "receiveDate", ignore = true)
    @Mapping(target = "merge", ignore = true)
    PlatformAwdShipmentReceiveDTO convertToPlatformAwdShipmentReceiveDTO(DmpAwdShipmentDetailEntity dmpDetailEntity);
}
