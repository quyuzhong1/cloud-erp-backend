package com.erp.server.sys.convert;

import com.common.business.dto.PlatformCityDictDTO;
import com.erp.model.sys.entity.DictThirdCity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface CityDictConvert {

    CityDictConvert INSTANCE = Mappers.getMapper(CityDictConvert.class);

    @Mappings({
            @Mapping(target = "regionName", source = "regionName"),
            @Mapping(target = "countryCode", constant = "CN"),
            @Mapping(target = "parentRegionId", source = "parentRegionId"),
            @Mapping(target = "regionLevel", source = "regionLevel"),
            @Mapping(target = "regionId", source = "regionId"),
            @Mapping(target = "platform", source = "provider"),
            @Mapping(target = "disabled", constant = "false")
    })
    DictThirdCity imlConversion(PlatformCityDictDTO dto);

}
