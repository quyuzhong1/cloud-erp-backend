package com.erp.server.wms.convert;

import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.damai.dto.response.DaMaiGetFbaOrderResp;
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

    @Mapping(target = "attachList", ignore = true)
    @Mapping(target = "warehouseOperationTypeName", expression = "java(com.erp.model.wms.enums.WarehouseOperationTypeEnum.getName(entity.getWarehouseOperationType()))")
    @Mapping(target = "statusName", expression = "java(com.erp.model.wms.enums.ThirdDeliveryStatusEnum.getName(entity.getWarehouseOperationType()))")
    @Mapping(target = "deliveryMethodName", expression = "java(com.erp.model.wms.enums.DeliveryMethodEnum.TRUCK_SELF.getName())")
    @Mapping(target = "detailList", source = "detailEntityList")
    B2bThirdDeliveryDTO.ViewDTO toB2bThirdDeliveryViewDTO(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList);
    B2bThirdDeliveryDetailDTO.ViewDTO toB2bThirdDeliveryDetailViewDTO(B2bThirdDeliveryDetailEntity deliveryDetail);

    List<B2bThirdDeliveryDetailEntity> toB2bThirdDeliveryDetail(List<B2bThirdDeliveryDetailDTO.AddDTO> detailList);
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    B2bThirdDeliveryDetailEntity toB2bThirdDeliveryDetail(B2bThirdDeliveryDetailDTO.AddDTO detail);

    @Mapping(target = "telNumberExt", ignore = true)
    @Mapping(target = "shortName", ignore = true)
    @Mapping(target = "sourceId", source = "entity.id")
    @Mapping(target = "sourceCode", source = "entity.code")
    @Mapping(target = "referenceNo", source = "entity.code")
    @Mapping(target = "receiverCountryCode", source = "entity.countryId")
    @Mapping(target = "platformShipNo", constant = "N/A")
    @Mapping(target = "platformRefNo", constant = "N/A")
    @Mapping(target = "items", source = "detailEntityList")
    @Mapping(target = "houseNumber", ignore = true)
    @Mapping(target = "fileUrl", ignore = true)
    @Mapping(target = "fbaAddressFlag", constant = "0")
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "deliveryType", source = "entity.deliveryMethod")
    @Mapping(target = "channelCode", source = "entity.logisticsChannelCode")
    @Mapping(target = "address3", ignore = true)
    @Mapping(target = "address2", ignore = true)
    @Mapping(target = "address1", source = "entity.receiveAddress")
    ThirdWarehouseCreateFbaOutboundReq toCreateFbaOutboundReq(B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList);

    ThirdWarehouseCreateFbaOutboundReq.Item toCreateFbaOutboundReqItem(B2bThirdDeliveryDetailEntity entity);

    List<ThirdWarehouseQueryFbaOutboundResponse> toB2bThirdDeliveryQueryDTO(List<DaMaiGetFbaOrderResp> dataList);
    @Mapping(target = "trackNo", source = "mainTrackingNo")
    @Mapping(target = "platformOrderCode", source = "fbaSoNo")
    @Mapping(target = "errorType", source = "seType")
    @Mapping(target = "deliveryTimeStr", source = "confirmTime")
    @Mapping(target = "code", source = "custRefNo")
    ThirdWarehouseQueryFbaOutboundResponse toB2bThirdDeliveryQueryDTO(DaMaiGetFbaOrderResp data);
}
