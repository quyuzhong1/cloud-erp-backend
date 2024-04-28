package com.erp.server.wms.convert;

import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
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
}
