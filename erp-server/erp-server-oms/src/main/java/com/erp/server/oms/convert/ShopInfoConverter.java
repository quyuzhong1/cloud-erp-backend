package com.erp.server.oms.convert;

import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ShopInfoConverter {
    ShopInfoConverter INSTANCE = Mappers.getMapper(ShopInfoConverter.class);

    /**
     * 店铺下拉
     * @param shopInfoEntity
     * @return
     */
    @Mappings({
            @Mapping(target = "code", source = "id"),
            @Mapping(target = "value", source = "name"),
            @Mapping(target = "disabled", source = "disabled"),
    })
    BaseDropDownDTO.DisabledDTO ShopInfoEntityToDisabledDTO(ShopInfoEntity shopInfoEntity);
    List<BaseDropDownDTO.DisabledDTO> ShopInfoEntityToDisabledDTO(List<ShopInfoEntity> shopInfoEntity);
}
