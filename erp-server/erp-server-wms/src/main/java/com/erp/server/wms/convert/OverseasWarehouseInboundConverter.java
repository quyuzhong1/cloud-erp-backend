package com.erp.server.wms.convert;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.mapper.DateMapperWork;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateOutboundReq;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.antu.dto.request.AntuCreateInboundReq;
import com.sdk.wms.antu.dto.request.AntuCreateOutboundReq;
import com.sdk.wms.goodcang.dto.request.GoodCangCreateInboundReq;
import com.sdk.wms.goodcang.dto.request.GoodCangCreateOutboundReq;
import com.sdk.wms.iml.dto.request.ImlCreateInboundReq;
import com.sdk.wms.iml.dto.request.ImlCreateOutboundReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
            @Mapping(target = "imagesUrl", expression = "java(cn.hutool.core.text.CharSequenceUtil.isBlank(imageUrl)? \"\" : imageUrl)"),
    })
    OverseasWarehouseInboundDetailDTO.ViewDTO detailEntityToViewDTO(OverseasWarehouseInboundDetailEntity entity, String imageUrl);

    @Mappings({
            @Mapping(target = "detailId",  source = "entity.id"),
            @Mapping(target = "id",  source = "entity.mainId"),
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
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "mainId",  source = "mainEntity.id"),
            @Mapping(target = "createTime", source = "createTime"),
            @Mapping(target = "createUserId", source = "createUserId"),
            @Mapping(target = "createUserName", source = "createUserName"),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "platformProductName",  source = "platformProductName"),
            @Mapping(target = "platformSkuNo",  source = "detailEntity.platformSkuNo"),
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
                                                                String platformProductName,
                                                                String createUserId, String createUserName, LocalDateTime createTime);

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
            @Mapping(target = "smCode",  expression = "java(OverseasWarehouseInboundConverter.getImlSmCode(sourceData))"),
            @Mapping(target = "trackingNumber",  source = "trackingNumber"),
            @Mapping(target = "etaDate",  source = "etaDate",qualifiedByName = "toStrByDate"),
            @Mapping(target = "verify",  source = "verify"),
            @Mapping(target = "contacter",  source = "collect.contacterName"),
            @Mapping(target = "contactPhone",  source = "collect.contactPhone"),
            @Mapping(target = "regionIdLevel0",  source = "collect.collectStateId"),
            @Mapping(target = "regionIdLevel1",  source = "collect.collectCityId"),
            @Mapping(target = "regionIdLevel2",  source = "collect.collectAreaId"),
            @Mapping(target = "customerType",  source = "declareType"),
            @Mapping(target = "street",  source = "collect.collectStreet"),
            @Mapping(target = "items",  source = "items"),
    })
    ImlCreateInboundReq inboundDtoToIml(ThirdWarehouseCreateInboundReq sourceData);

    static String getImlSmCode(ThirdWarehouseCreateInboundReq data){
        if(OverseasInstockTypeEnum.TRANSFER_AGENT.getCode().equals(data.getReceivingType())){
            return CharSequenceUtil.isBlank(data.getSmCode()) ? "PHLY1" : data.getSmCode();
        }
        return null;
    }

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "boxNo",  source = "boxNo"),
            @Mapping(target = "quantity",  source = "quantity"),
    })
    ImlCreateInboundReq.Item inboundDtoToImlItem(ThirdWarehouseCreateInboundReq.Item createInboundReq);

    @Mappings({
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "orderDesc",  source = "platformCode"),
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
            @Mapping(target = "zipcode",  source = "receiverInfo.zipCode"),
            @Mapping(target = "itemList",  source = "items"),
    })
    GoodCangCreateOutboundReq outboundDtoToGoodCang(ThirdWarehouseCreateOutboundReq createOutboundReq);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "quantity",  source = "quantity"),
            @Mapping(target = "hsCode",  source = "hsCode"),
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
            @Mapping(target = "zipcode",  source = "receiverInfo.zipCode"),
            @Mapping(target = "items",  source = "items"),
    })
    ImlCreateOutboundReq outboundDtoToIml(ThirdWarehouseCreateOutboundReq createOutboundReq);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "quantity",  source = "quantity"),
    })
    ImlCreateOutboundReq.Item outboundDtoToIml(ThirdWarehouseCreateOutboundReq.Item createOutboundReq);

    @Mappings({
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "swOrderNumber",  source = "platformCode"),
            @Mapping(target = "shippingMethod",  source = "shippingMethod"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "verify",  source = "verify",defaultValue = "1"),
            @Mapping(target = "countryCode",  source = "receiverInfo.countryCode"),
            @Mapping(target = "province",  source = "receiverInfo.province"),
            @Mapping(target = "city",  source = "receiverInfo.city"),
            @Mapping(target = "district",  source = "receiverInfo.district"),
            @Mapping(target = "address1",  source = "receiverInfo.address1"),
            @Mapping(target = "address2",  source = "receiverInfo.address2"),
            @Mapping(target = "address3",  source = "receiverInfo.address3"),
            @Mapping(target = "zipcode",  source = "receiverInfo.zipCode"),
            @Mapping(target = "license",  source = "receiverInfo.taxNumber"),
            @Mapping(target = "name",  source = "receiverInfo.name"),
            @Mapping(target = "phone",  source = "receiverInfo.phone"),
            @Mapping(target = "doorplate",  constant = "0"),
            @Mapping(target = "email",  source = "receiverInfo.email"),
            @Mapping(target = "items",  source = "items"),
    })
    AntuCreateOutboundReq outboundDtoToAntu(ThirdWarehouseCreateOutboundReq createOutboundReq);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "quantity",  source = "quantity"),
    })
    AntuCreateOutboundReq.Item outboundDtoToAntu(ThirdWarehouseCreateOutboundReq.Item createOutboundReq);


    @Mappings({
            @Mapping(target = "receivingCode",  source = "receivingCode"),
            @Mapping(target = "referenceNo",  source = "referenceNo"),
            @Mapping(target = "incomeType",  expression = "java(com.sdk.wms.antu.enums.AntuEnums.IncomeTypeEnum.getCodeByErp(sourceData.getIncomeType()))"),
            @Mapping(target = "receivingType",  expression = "java(com.sdk.wms.antu.enums.AntuEnums.TransitTypeEnum.getCodeByErp(sourceData.getReceivingType()))"),
            @Mapping(target = "warehouseCode",  source = "warehouseCode"),
            @Mapping(target = "transitWarehouseCode",  source = "transitWarehouseCode"),
            @Mapping(target = "smCode",  source = "smCode"),
            @Mapping(target = "trackingNumber",  source = "trackingNumber"),
            @Mapping(target = "etaDate",  source = "etaDate",qualifiedByName = "toStrByDate"),
            @Mapping(target = "verify",  source = "verify"),
            @Mapping(target = "contacter",  source = "collect.contacterName"),
            @Mapping(target = "contactPhone",  source = "collect.contactPhone"),
            @Mapping(target = "regionIdLevel0",  source = "collect.collectStateId"),
            @Mapping(target = "regionIdLevel1",  source = "collect.collectCityId"),
            @Mapping(target = "regionIdLevel2",  source = "collect.collectAreaId"),
            @Mapping(target = "customerType",  source = "declareType"),
            @Mapping(target = "street",  source = "collect.collectStreet"),
            @Mapping(target = "items",  source = "items"),
    })
    AntuCreateInboundReq inboundDtoToAntu(ThirdWarehouseCreateInboundReq sourceData);

    @Mappings({
            @Mapping(target = "productSku",  source = "productSku"),
            @Mapping(target = "boxNo",  source = "boxNo"),
            @Mapping(target = "quantity",  source = "quantity"),
    })
    AntuCreateInboundReq.Item inboundDtoToAntuItem(ThirdWarehouseCreateInboundReq.Item createInboundReq);
    @Mappings({
            @Mapping(target = "category", ignore = true),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.RECEIVE_QTY.getCode())"),
            @Mapping(target = "deliveryCode", source = "entity.sourceCode"),
            @Mapping(target = "deliveryId", source = "entity.sourceId"),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "receivedEntity.receiveQty"),
            @Mapping(target = "oldValue", ignore = true),
            @Mapping(target = "reportPeriod", ignore = true),
            @Mapping(target = "reportPeriodId", ignore = true),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.THIRDRECEIVE.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "sourceId", source = "entity.id"),
            @Mapping(target = "changeRange", ignore = true),
            @Mapping(target = "boxId", ignore = true),
            @Mapping(target = "businessCode", source = "entity.code"),
            @Mapping(target = "logisticsBillId", ignore = true),
            @Mapping(target = "transportNo", ignore = true)
    })
    FirstMileChangeRecordDTO.AddDTO convertOverseasToChangeRecord(OverseasWarehouseInboundEntity entity, OverseasWarehouseInboundDetailEntity detailEntity, OverseasWarehouseInboundReceivedEntity receivedEntity);
    @Mappings({
            @Mapping(target = "category", ignore = true),
            @Mapping(target = "categoryField", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordCategoryFieldEnum.RECEIVE_QTY.getCode())"),
            @Mapping(target = "deliveryCode", source = "deliveryCode"),
            @Mapping(target = "deliveryId", ignore = true),
            @Mapping(target = "isLatest", constant = "true"),
            @Mapping(target = "newValue", source = "receivedEntity.receiveQty"),
            @Mapping(target = "oldValue", ignore = true),
            @Mapping(target = "reportPeriod", ignore = true),
            @Mapping(target = "reportPeriodId", ignore = true),
            @Mapping(target = "sourceType", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordSourceTypeEnum.THIRDRECEIVE.getCode())"),
            @Mapping(target = "type", expression = "java(com.erp.model.tms.enums.FirstMileChangeRecordTypeEnum.MANUAL.getCode())"),
            @Mapping(target = "sourceId", source = "entity.id"),
            @Mapping(target = "changeRange", ignore = true),
            @Mapping(target = "boxId", ignore = true),
            @Mapping(target = "businessCode", source = "entity.code"),
            @Mapping(target = "logisticsBillId", ignore = true),
            @Mapping(target = "transportNo", ignore = true),
            @Mapping(target = "platformSkuNo", source = "detailEntity.fnSku"),
            @Mapping(target = "skuId", source = "detailEntity.skuId"),
            @Mapping(target = "skuNo", source = "detailEntity.skuNo")
    })
    FirstMileChangeRecordDTO.AddDTO convertFbaToChangeRecord(FbaShipmentEntity entity, FbaShipmentDetailEntity detailEntity, FbaShipmentReceiveEntity receivedEntity, String deliveryCode);
}
