package com.erp.server.wms.convert;

import com.erp.model.wms.dto.SoB2cProcessingDTO;
import com.erp.model.wms.entity.SoB2cProcessingEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * b2c虚拟仓订单
 * @author will
 * @date 2025/1/3 9:10
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface SoB2cProcessingConverter {
    SoB2cProcessingConverter INSTANCE = Mappers.getMapper(SoB2cProcessingConverter.class);

    SoB2cProcessingDTO.AddOrUpdateDTO entityToAdd(SoB2cProcessingEntity entity);

    SoB2cProcessingEntity addToEntity(SoB2cProcessingDTO.AddOrUpdateDTO addOrUpdateDTO);

}
