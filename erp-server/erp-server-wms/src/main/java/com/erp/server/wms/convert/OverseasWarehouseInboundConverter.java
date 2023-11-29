package com.erp.server.wms.convert;

import com.common.business.mapper.DateMapperWork;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.goodcang.dto.request.GoodCangCreateInboundReq;
import com.sdk.wms.iml.dto.request.ImlCreateInboundReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * 海外仓入库单
 **/
@Mapper(uses = {TypeConversionWorker.class, DateMapperWork.class})
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

    @Mappings({
            @Mapping(target = "receivingCode",  source = "receivingCode"),
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "transitType",  source = "transitType"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "transitWarehouseCode",  source = "transitWarehouseCode"),
            @Mapping(target = "smCode",  source = "smCode"),
            @Mapping(target = "receivingShippingType",  source = "receivingShippingType"),
            @Mapping(target = "trackingNumber",  source = "trackingNumber"),
            @Mapping(target = "etaDate",  source = "etaDate"),
            @Mapping(target = "verify",  source = "verify"),
            @Mapping(target = "customsType",  source = "customsType"),
            @Mapping(target = "collectingService",  source = "collectingService"),
            @Mapping(target = "customersSendInfo.deliveryCode",  source = "deliveryCode"),
            @Mapping(target = "collectingTime",  source = "collect.collectingTime"),
            @Mapping(target = "clearanceService",  source = "clearanceService"),
            @Mapping(target = "shiperAddress.saContacter",  source = "shiperInfo.contacterName"),
            @Mapping(target = "shiperAddress.saContactPhone",  source = "shiperInfo.phone"),
            @Mapping(target = "shiperAddress.saCountryCode",  source = "shiperInfo.countryCode"),
            @Mapping(target = "shiperAddress.saState",  source = "shiperInfo.stateName"),
            @Mapping(target = "shiperAddress.saCity",  source = "shiperInfo.cityName"),
            @Mapping(target = "shiperAddress.saRegion",  source = "shiperInfo.region"),
            @Mapping(target = "shiperAddress.saAddress1",  source = "shiperInfo.address1"),
            @Mapping(target = "shiperAddress.saAddress2",  source = "shiperInfo.address2"),
            @Mapping(target = "items",  source = "items"),
    })
    GoodCangCreateInboundReq inboundDtoToGoodCang(ThirdWarehouseCreateInboundReq createInboundReq);

    @Mappings({
            @Mapping(target = "caFirstName",  source = "collect.contacterFirstName"),
            @Mapping(target = "caLastName",  source = "collect.contacterLastName"),
            @Mapping(target = "caContactPhone",  source = "collect.contactPhone"),
            @Mapping(target = "caCountryCode",  source = "collect.collectCountryCode"),
            @Mapping(target = "caState",  source = "collect.collectStateName"),
            @Mapping(target = "caCity",  source = "collect.collectCityName"),
            @Mapping(target = "caZipcode",  source = "collect.collectZipcode"),
            @Mapping(target = "caAddress1",  source = "collect.collectStreet"),
            @Mapping(target = "caAddress2",  source = "collect.collectStreet2"),
    })
    GoodCangCreateInboundReq.CollectingAddress inboundDtoToGoodCangCollect(ThirdWarehouseCreateInboundReq createInboundReq);

    @Mappings({
            @Mapping(target = "receivingCode",  source = "receivingCode"),
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "incomeType",  source = "incomeType"),
            @Mapping(target = "receivingType",  source = "receivingType"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "transitWarehouseCode",  source = "transitWarehouseCode"),
            @Mapping(target = "smCode",  source = "smCode"),
            @Mapping(target = "trackingNumber",  source = "trackingNumber"),
            @Mapping(target = "etaDate",  source = "etaDate",qualifiedByName = "localDateTimeToDate"),
            @Mapping(target = "verify",  source = "verify"),
            @Mapping(target = "contacter",  source = "collect.contacterName"),
            @Mapping(target = "contactPhone",  source = "collect.contactPhone"),
            @Mapping(target = "regionIdLevel0",  source = "collect.collectStateId"),
            @Mapping(target = "regionIdLevel1",  source = "collect.collectCityId"),
            @Mapping(target = "regionIdLevel2",  source = "collect.collectAreaId"),
            @Mapping(target = "street",  source = "collect.collectStreet"),
            @Mapping(target = "items",  source = "items"),
    })
    ImlCreateInboundReq inboundDtoToIml(ThirdWarehouseCreateInboundReq createInboundReq);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "boxNo",  source = "boxNo"),
            @Mapping(target = "quantity",  source = "quantity"),
    })
    ImlCreateInboundReq.Item inboundDtoToImlItem(ThirdWarehouseCreateInboundReq.Item createInboundReq);
}
