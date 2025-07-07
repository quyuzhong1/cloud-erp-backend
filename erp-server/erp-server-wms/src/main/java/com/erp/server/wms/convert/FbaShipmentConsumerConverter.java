package com.erp.server.wms.convert;

import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.entity.FbaShipmentStatusEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;


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
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "mainId", source = "shipmentEntity.id"),
            @Mapping(target = "asin", source = "mappingDTO.platformSpuNo"),
            @Mapping(target = "msku", source = "receiveDTO.sellerSku", defaultValue = ""),
            @Mapping(target = "fnSku", source = "receiveDTO.fnSku", defaultValue = ""),
            @Mapping(target = "skuNo", source = "mappingDTO.productSkuNo", defaultValue = ""),
            @Mapping(target = "skuId", source = "mappingDTO.productSkuId", defaultValue = ""),
            @Mapping(target = "declareQty", source = "receiveDTO.declareQty"),
            @Mapping(target = "diffQty",constant = "0"),
            @Mapping(target = "receiveQty", ignore = true),
            @Mapping(target = "receiveDate", expression = "java(receiveDTO.getReceiveQty() > 0 ? receiveDTO.getReceiveDate() : null)"),
    })
    FbaShipmentDetailEntity fbaShipmentToDetailEntity(
            PlatformFbaShipmentReceiveDTO receiveDTO,
            FbaShipmentEntity shipmentEntity,
            ListingInfoWithSkuMappingDTO mappingDTO);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "msku", source = "receiveDTO.sellerSku", defaultValue = ""),
            @Mapping(target = "detailId", source = "detailId"),
            @Mapping(target = "fnSku", source = "receiveDTO.fnSku", defaultValue = ""),
            @Mapping(target = "skuNo", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\" :listingInfoWithSkuMappingDTO.checkAndGetProductSkuNo())"),
            @Mapping(target = "asin", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\" :listingInfoWithSkuMappingDTO.checkAndGetProductSpuNo())"),
            @Mapping(target = "skuId", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\" :listingInfoWithSkuMappingDTO.checkAndGetProductSkuId())"),
            @Mapping(target = "declareQty", source = "receiveDTO.declareQty"),
            @Mapping(target = "receiveQty", source = "receiveDTO.receiveQty"),
            @Mapping(target = "fbaShipmentId", source = "shipmentEntity.fbaShipmentId"),
            @Mapping(target = "shopId", source = "shipmentEntity.shopId"),
            @Mapping(target = "receiveDate",  expression = "java(null == receiveDTO.getReceiveDate() ? java.time.LocalDateTime.now() :receiveDTO.getReceiveDate())"),
    })
    FbaShipmentReceiveEntity fbaShipmentToReceiveEntity(
            String detailId,
            PlatformFbaShipmentReceiveDTO receiveDTO,
            FbaShipmentEntity shipmentEntity,
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO);


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


    @Mappings({
            @Mapping(target = "skuId", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\" :listingInfoWithSkuMappingDTO.checkAndGetProductSkuId())"),
            @Mapping(target = "skuNo", expression = "java(null == listingInfoWithSkuMappingDTO ? \"\" :listingInfoWithSkuMappingDTO.checkAndGetProductSkuNo())"),
            @Mapping(target = "isCombination", expression = "java(null != listingInfoWithSkuMappingDTO && hasChildrenSkuIds.contains(listingInfoWithSkuMappingDTO.getProductSkuId()))"),
    })
    FbaShipmentDetailEntity detailSetSkuMappingInfo(FbaShipmentDetailEntity detailEntity,
                                                    ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO,
                                                    List<String> hasChildrenSkuIds);
}
