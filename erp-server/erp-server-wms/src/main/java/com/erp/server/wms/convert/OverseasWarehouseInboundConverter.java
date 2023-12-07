package com.erp.server.wms.convert;

import com.common.business.mapper.DateMapperWork;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.goodcang.dto.request.GoodCangCreateInboundReq;
import com.sdk.wms.goodcang.dto.request.GoodCangCreateOutboundReq;
import com.sdk.wms.iml.dto.request.ImlCreateInboundReq;
import com.sdk.wms.iml.dto.request.ImlCreateOutboundReq;
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
            @Mapping(target = "receiveTime", expression = "java(null == entity.getReceiveTime() ? null : entity.getReceiveTime().toLocalDate())"),
            @Mapping(target = "estimatedArrivalDate", expression = "java(null == entity.getEstimatedArrivalDate() ? null : entity.getEstimatedArrivalDate().toLocalDate())"),
    })
    OverseasWarehouseInboundDTO.ViewDTO entityToViewDTO(OverseasWarehouseInboundEntity entity);


    @Mappings({
            @Mapping(target = "imagesUrl", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(imageUrl)? \"\" : imageUrl)"),
    })
    OverseasWarehouseInboundDetailDTO.ViewDTO detailEntityToViewDTO(OverseasWarehouseInboundDetailEntity entity, String imageUrl);

    @Mappings({
            @Mapping(target = "detailId",  source = "entity.id"),
            @Mapping(target = "code",  source = "mainEntity.code"),
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

            @Mapping(target = "platformProductName",  expression = "java(null == skuMappingView ? \"\" : skuMappingView.getStockSkuName())"),
            @Mapping(target = "platformSkuNo",  expression = "java(null == skuMappingView ? \"\" : skuMappingView.getStockSku())"),
            @Mapping(target = "productName",  source = "detailEntity.productName"),
            @Mapping(target = "skuNo",  source = "detailEntity.skuNo"),
            @Mapping(target = "skuId",  source = "detailEntity.skuId"),
            @Mapping(target = "isCombination",  source = "detailEntity.isCombination"),
            @Mapping(target = "receiveQty",  constant = "0"),
            @Mapping(target = "packQty",  source = "detailEntity.deliveryQty"),
            @Mapping(target = "transportQty",  source = "detailEntity.deliveryQty"),
            @Mapping(target = "diffQty",  expression = "java(-detailEntity.getDeliveryQty())"),
            @Mapping(target = "receiveStatus",  constant = "not"),
            @Mapping(target = "receiveType",  constant = ""),
    })
    OverseasWarehouseInboundDetailEntity deliveryDetailToDetail(FirstMileDeliveryDetailEntity detailEntity,
                                                                OverseasWarehouseInboundEntity mainEntity,
                                                                SkuMappingDTO.ListStockSkuNoByProductSkuIdView skuMappingView
    );

    @Mappings({
            @Mapping(target = "receivingCode",  source = "receivingCode"),
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "transitType",  expression = "java(com.sdk.wms.goodcang.enums.GoodCangEnums.OpenTransitTypeEnum.getCodeByErp(sourceData.getTransitType()))"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "transitWarehouseCode",  source = "transitWarehouseCode"),
            @Mapping(target = "smCode",  source = "smCode"),
            @Mapping(target = "receivingShippingType",  expression = "java(com.sdk.wms.goodcang.enums.GoodCangEnums.ProductCodeEnum.getCodeByErp(sourceData.getReceivingShippingType()))"),
            @Mapping(target = "trackingNumber",  source = "trackingNumber"),
            @Mapping(target = "etaDate",  source = "etaDate"),
            @Mapping(target = "verify",  source = "verify"),
            @Mapping(target = "customsType",  expression = "java(com.sdk.wms.goodcang.enums.GoodCangEnums.CustomsTypeNewEnum.getCodeByErp(sourceData.getCustomsType()))"),
            @Mapping(target = "collectingService",  expression = "java(com.sdk.wms.goodcang.enums.GoodCangEnums.OpenCollectingServiceEnum.getCodeByErp(sourceData.getCollectingService()))"),
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
    GoodCangCreateInboundReq inboundDtoToGoodCang(ThirdWarehouseCreateInboundReq sourceData);

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
            @Mapping(target = "incomeType",  expression = "java(com.sdk.wms.iml.enums.ImlEnums.IncomeTypeEnum.getCodeByErp(sourceData.getIncomeType()))"),
            @Mapping(target = "receivingType",  expression = "java(com.sdk.wms.iml.enums.ImlEnums.TransitTypeEnum.getCodeByErp(sourceData.getReceivingType()))"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "transitWarehouseCode",  source = "transitWarehouseCode"),
            @Mapping(target = "smCode",  source = "smCode" ,defaultValue = "PHLY1"),
            @Mapping(target = "trackingNumber",  source = "trackingNumber"),
            @Mapping(target = "etaDate",  source = "etaDate",qualifiedByName = "toStrByDate"),
            @Mapping(target = "verify",  source = "verify"),
            @Mapping(target = "contacter",  source = "collect.contacterName"),
            @Mapping(target = "contactPhone",  source = "collect.contactPhone"),
            @Mapping(target = "regionIdLevel0",  source = "collect.collectStateId"),
            @Mapping(target = "regionIdLevel1",  source = "collect.collectCityId"),
            @Mapping(target = "regionIdLevel2",  source = "collect.collectAreaId"),
            @Mapping(target = "street",  source = "collect.collectStreet"),
            @Mapping(target = "items",  source = "items"),
    })
    ImlCreateInboundReq inboundDtoToIml(ThirdWarehouseCreateInboundReq sourceData);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "boxNo",  source = "boxNo"),
            @Mapping(target = "quantity",  source = "quantity"),
    })
    ImlCreateInboundReq.Item inboundDtoToImlItem(ThirdWarehouseCreateInboundReq.Item createInboundReq);

    @Mappings({
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "shippingMethod",  source = "shippingMethod"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "verify",  source = "verify",defaultValue = "0"),
            @Mapping(target = "name",  source = "receiverInfo.name"),
            @Mapping(target = "phone",  source = "receiverInfo.phone"),
            @Mapping(target = "countryCode",  source = "receiverInfo.countryCode"),
            @Mapping(target = "province",  source = "receiverInfo.province"),
            @Mapping(target = "city",  source = "receiverInfo.city"),
            @Mapping(target = "address1",  source = "receiverInfo.address1"),
            @Mapping(target = "address2",  source = "receiverInfo.address2"),
            @Mapping(target = "zipcode",  source = "receiverInfo.zipcode"),
            @Mapping(target = "itemList",  source = "items"),
    })
    GoodCangCreateOutboundReq outboundDtoToGoodCang(ThirdWarehouseCreateOutboundReq createOutboundReq);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "quantity",  source = "quantity"),
    })
    GoodCangCreateOutboundReq.Item outboundDtoToGoodCang(ThirdWarehouseCreateOutboundReq.Item createOutboundReq);


    @Mappings({
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "shippingMethod",  source = "shippingMethod"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "verify",  source = "verify",defaultValue = "1"),
            @Mapping(target = "name",  source = "receiverInfo.name"),
            @Mapping(target = "phone",  source = "receiverInfo.phone"),
            @Mapping(target = "countryCode",  source = "receiverInfo.countryCode"),
            @Mapping(target = "province",  source = "receiverInfo.province"),
            @Mapping(target = "city",  source = "receiverInfo.city"),
            @Mapping(target = "address1",  source = "receiverInfo.address1"),
            @Mapping(target = "address2",  source = "receiverInfo.address2"),
            @Mapping(target = "address3",  source = "receiverInfo.address3"),
            @Mapping(target = "zipcode",  source = "receiverInfo.zipcode"),
            @Mapping(target = "items",  source = "items"),
    })
    ImlCreateOutboundReq outboundDtoToIml(ThirdWarehouseCreateOutboundReq createOutboundReq);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "quantity",  source = "quantity"),
    })
    ImlCreateOutboundReq.Item outboundDtoToIml(ThirdWarehouseCreateOutboundReq.Item createOutboundReq);

}
