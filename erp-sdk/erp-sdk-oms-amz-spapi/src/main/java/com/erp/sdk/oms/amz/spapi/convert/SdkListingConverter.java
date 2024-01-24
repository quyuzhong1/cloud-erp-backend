package com.erp.sdk.oms.amz.spapi.convert;

import com.erp.sdk.oms.amz.spapi.csv.ReportListingCsvEntity;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonListingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 亚马逊货件转换器
 *
 * @author Jim
 * @since 2023-11-01
 **/
@Mapper
@Component
public interface SdkListingConverter {

    SdkListingConverter INSTANCE = Mappers.getMapper(SdkListingConverter.class);


    @Mappings({
            @Mapping(target = "uniqueId", source = "csvEntity.listingId"),
            @Mapping(target = "shopId", source = "shopId"),
            @Mapping(target = "downloadStatus", expression = "java(null == csvEntity.getAsin1() ? -1 : 0)"),
            @Mapping(target = "platformUpdateTime", source = "updateTime"),
    })
    PlatformAmazonListingDTO mongoDtoToListingDto(ReportListingCsvEntity csvEntity, String shopId, LocalDateTime updateTime);
}
