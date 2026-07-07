package com.erp.server.wms.convert;

import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.erp.tms.aliexpress.model.handover.AddressBase;
import com.erp.tms.aliexpress.model.handover.AddressInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lambda
 * @Classname PackageForecastConverter
 * @Description TODO
 * @Date 2024-02-04 14:10
 * @Created by yl
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface PackageForecastConverter {
    PackageForecastConverter INSTANCE = Mappers.getMapper(PackageForecastConverter.class);


    @Mappings({
            @Mapping(target = "soId", source = "soId"),
            @Mapping(target = "soCode", source = "soCode"),
            @Mapping(target = "logisticsChannelId", source = "logisticsChannelId"),
            @Mapping(target = "trackNo", source = "transportNo"),
            @Mapping(target = "packageWeight", source = "weight"),
            @Mapping(target = "weightUnit", source = "weightUnit"),
    })
    TransferDeclareDetailDTO.AddDTO convertDeclareDetail(PackageForecastDetailEntity entity);
    List<TransferDeclareDetailDTO.AddDTO> convertDeclareDetail(List<PackageForecastDetailEntity> list);


    @Mappings({
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "mobile", source = "telNumber"),
            @Mapping(target = "phone", source = "telNumber"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "addressId", source = "addressId"),
    })
    AddressInfo convertAddressInfo(LogisticsAddressEntity entity);

    @Mappings({
            @Mapping(target = "zipCode", source = "zipCode"),
            @Mapping(target = "detailAddress", source = "addressFirst"),
            @Mapping(target = "street", source = "street"),
            @Mapping(target = "district", source = "districtName"),
            @Mapping(target = "city", source = "cityName"),
            @Mapping(target = "province", source = "provinceName"),
            @Mapping(target = "country", source = "country"),
    })
    AddressBase convertAddressBase(LogisticsAddressEntity logisticsAddress);


    @Mapping(target = "detailId", source = "id")
    @Mapping(target = "minPackageTransportNo", source = "transportNo")
    @Mapping(target = "minPackageHandoverStatusName", expression = "java(com.erp.model.wms.enums.HandoverSubStatusEnum.getByCode(entity.getHandoverStatus()))")
    @Mapping(target = "minPackageHandoverStatus", source = "handoverStatus")
    @Mapping(target = "outstockStatusName", expression = "java(cn.hutool.core.text.CharSequenceUtil.isNotEmpty(entity.getSoId()) ? \"已出库\" : \"未出库\")")
    @Mapping(target = "trackNo", expression = "java(cn.hutool.core.text.CharSequenceUtil.isBlank(entity.getTrackNo()) ? entity.getTransportNo() : entity.getTrackNo())")
    @Mapping(target = "weightStr", expression = "java(entity.getWeight() + entity.getWeightUnit())")
    PackageForecastDTO.PagingDetailViewDTO convertPagingDetailView(PackageForecastDetailEntity entity);
    List<PackageForecastDTO.PagingDetailViewDTO> convertPagingDetailViewList(List<PackageForecastDetailEntity> detailEntities);
}
