package com.erp.server.oms.convert;

import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.entity.SkuMappingExtendEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface SkuMappingConverter {
    SkuMappingConverter INSTANCE = Mappers.getMapper(SkuMappingConverter.class);

    SkuMappingEntity copySkuMappingEntity(SkuMappingEntity entity);

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
    })
    SkuMappingExtendEntity copySkuMappingExtendEntity(SkuMappingExtendEntity entity);
}
