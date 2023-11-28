package com.erp.server.wms.convert;

import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.goodcang.dto.request.GoodCangCreateInboundReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * 海外仓入库单
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface OverseasWarehouseInboundConverter {

    OverseasWarehouseInboundConverter INSTANCE = Mappers.getMapper(OverseasWarehouseInboundConverter.class);

    @Mappings({
//            @Mapping(target = "id",  ignore = true)
    })
    OverseasWarehouseInboundDTO.ViewDTO entityToViewDTO(OverseasWarehouseInboundEntity entity);


    @Mappings({
            @Mapping(target = "imagesUrl", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(imageUrl)? \"\" : imageUrl)"),
    })
    OverseasWarehouseInboundDetailDTO.ViewDTO detailEntityToViewDTO(OverseasWarehouseInboundDetailEntity entity, String imageUrl);

    @Mappings({
            @Mapping(target = "id",  source = "entity.id"),
            @Mapping(target = "receiveTime",  source = "entity.receiveTime"),
            @Mapping(target = "sourceId", source = "mainEntity.sourceId"),
            @Mapping(target = "deliveryWarehouseName", source = "mainEntity.deliveryWarehouseName"),
            @Mapping(target = "deliveryWarehouseId", source = "mainEntity.deliveryWarehouseId"),
            @Mapping(target = "transferWarehouseName", source = "mainEntity.transferWarehouseName"),
            @Mapping(target = "transferWarehouseId", source = "mainEntity.transferWarehouseId"),
            @Mapping(target = "toWarehouseName", source = "mainEntity.toWarehouseName"),
            @Mapping(target = "toWarehouseId", source = "mainEntity.toWarehouseId"),
    })
    OverseasWarehouseInboundDetailDTO.ViewListDTO detailEntityToViewListDTO(OverseasWarehouseInboundDetailEntity entity, OverseasWarehouseInboundEntity mainEntity);


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
            @Mapping(target = "mainId",  source = "mainEntity.id"),
            // TODO 确定?
            @Mapping(target = "platformProductName",  source = "detailEntity.productName"),
            @Mapping(target = "platformSkuNo",  source = "detailEntity.stockSku"),
            @Mapping(target = "productName",  source = "detailEntity.productName"),
            @Mapping(target = "skuNo",  source = "detailEntity.skuNo"),
            @Mapping(target = "skuId",  source = "detailEntity.skuId"),
            @Mapping(target = "isCombination",  source = "detailEntity.isCombination"),
            @Mapping(target = "receiveQty",  constant = "0"),
            @Mapping(target = "transportQty",  constant = "0"),
            @Mapping(target = "packQty",  source = "detailEntity.deliveryQty"),
            @Mapping(target = "diffQty",  constant = "0"),
            @Mapping(target = "receiveStatus",  constant = "not"),
            @Mapping(target = "receiveType",  constant = ""),
    })
    OverseasWarehouseInboundDetailEntity deliveryDetailToDetail(FirstMileDeliveryDetailEntity detailEntity, OverseasWarehouseInboundEntity mainEntity);


    GoodCangCreateInboundReq inboundDtoToGoodCang(ThirdWarehouseCreateInboundReq createInboundReq);
}
