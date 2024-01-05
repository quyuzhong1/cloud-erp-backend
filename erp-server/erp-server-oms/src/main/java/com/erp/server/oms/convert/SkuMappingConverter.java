package com.erp.server.oms.convert;

import com.erp.model.oms.entity.SkuMappingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface SkuMappingConverter {
    SkuMappingConverter INSTANCE = Mappers.getMapper(SkuMappingConverter.class);

    SkuMappingEntity copySkuMappingEntity(SkuMappingEntity entity);
}
