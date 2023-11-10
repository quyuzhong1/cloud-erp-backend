package com.erp.server.wms.convert;

import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.entity.FbaShipmentStatusEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;


/**
 * FBA货件消费者映射工具
 *
 * @author Jim
 * @date 2023/11/2
 */
@Mapper()
@Component
public interface FbaShipmentConsumerConverter {
    FbaShipmentConsumerConverter INSTANCE = Mappers.getMapper(FbaShipmentConsumerConverter.class);


    @Mappings({
            @Mapping(target = "deliveryToAddress", constant = ""),
            @Mapping(target = "code", source = "fbaShipmentId"),
    })
    FbaShipmentEntity fbaShipmentToEntity(PlatformFbaShipmentDTO dto);

    @Mappings({
            @Mapping(target = "id",ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "mainId", source = "shipmentEntity.id"),
            @Mapping(target = "asin", source = "listingInfo.platformProductNo"),
            @Mapping(target = "msku", source = "receiveDTO.sellerSku", defaultValue = ""),
            @Mapping(target = "fnSku", source = "receiveDTO.fnSku", defaultValue = ""),
            @Mapping(target = "skuNo", source = "listingInfo.skuNo", defaultValue = ""),
            @Mapping(target = "declareQty", source = "receiveDTO.declareQty"),
            @Mapping(target = "diffQty", expression = "java(receiveDTO.calculateDiffQty())"),
//    @Mapping(target = "isCombination", source = ""),
            @Mapping(target = "receiveQty", source = "receiveDTO.receiveQty"),
            @Mapping(target = "receiveDate", source = "receiveDTO.receiveDate"),
    })
    FbaShipmentDetailEntity fbaShipmentToDetailEntity(
            PlatformFbaShipmentReceiveDTO receiveDTO,
            FbaShipmentEntity shipmentEntity,
            ListingInfoEntity listingInfo);

    @Mappings({
            @Mapping(target = "id",ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "asin", source = "listingInfo.platformProductNo"),
            @Mapping(target = "msku", source = "receiveDTO.sellerSku", defaultValue = ""),
            @Mapping(target = "fnSku", source = "receiveDTO.fnSku", defaultValue = ""),
            @Mapping(target = "skuNo", source = "listingInfo.skuNo", defaultValue = ""),
            @Mapping(target = "declareQty", source = "receiveDTO.declareQty"),
            @Mapping(target = "diffQty", expression = "java(receiveDTO.calculateDiffQty())"),
//    @Mapping(target = "isCombination", source = ""),
            @Mapping(target = "receiveQty", source = "receiveDTO.receiveQty"),
            @Mapping(target = "receiveDate", source = "receiveDTO.receiveDate"),
    })
    FbaShipmentReceiveEntity fbaShipmentToReceiveEntity(
            String detailId,
            PlatformFbaShipmentReceiveDTO receiveDTO,
            FbaShipmentEntity shipmentEntity,
            ListingInfoEntity listingInfo);



    @Mappings({
            @Mapping(target = "mainId", source = "id"),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
    })
    FbaShipmentStatusEntity fbaShipmentToStatusEntity(FbaShipmentEntity entity);
}
