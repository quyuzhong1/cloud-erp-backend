package com.erp.server.oms.convert;

import com.common.business.mapper.NumberMapperWork;
import com.common.business.mapper.ObjectMapperWork;
import com.erp.model.oms.dto.SoB2cCoreDTO;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 映射工具类
 * </p>
 *
 * @author Jim
 * @since 2023-11-15
 */

@Component
@Mapper(uses = {ObjectMapperWork.class,NumberMapperWork.class})
public interface SoB2cCoreConverter {
    SoB2cCoreConverter INSTANCE = Mappers.getMapper(SoB2cCoreConverter.class);


    @Mappings({
            @Mapping(target = "b2cSoId", source = "soB2cEntity.id"),
            @Mapping(target = "b2cSoCode", source = "soB2cEntity.code"),
            @Mapping(target = "b2cSoDetailId", source = "soB2cDetailEntity.id"),
            @Mapping(target = "platformCode", source = "soB2cEntity.platformCode"),
            @Mapping(target = "dictPlatform", source = "soB2cEntity.dictPlatform"),
            @Mapping(target = "skuId", source = "soB2cDetailEntity.skuId"),
            @Mapping(target = "skuNo", source = "soB2cDetailEntity.skuNo"),
            @Mapping(target = "warehouseId", source = "soB2cDetailEntity.warehouseId"),
            @Mapping(target = "warehouseLocation", source = "soB2cDetailEntity.warehouseLocation"),

    })
    SoB2cCoreDTO.ListRetryOutstockDTO convertSoB2cToRetryOutstock(SoB2cEntity soB2cEntity, SoB2cDetailEntity soB2cDetailEntity);


    @Mapping(target = "soB2cCode", ignore = true)
    SoB2cReceiverDTO.ViewDTO convertReceiverEntityToViewDTO(SoB2cReceiverEntity soB2cReceiverEntity);
}
