package com.erp.server.wms.convert;

import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.SoB2bProcessingEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * b2b虚拟仓订单
 * @author will
 * @date 2025/1/3 9:10
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface SoB2bProcessingConverter {
    SoB2bProcessingConverter INSTANCE = Mappers.getMapper(SoB2bProcessingConverter.class);


    SoB2bProcessingDTO.AddOrUpdateDTO entityToAdd(SoB2bProcessingEntity entity);

    SoB2bProcessingEntity addToEntity(SoB2bProcessingDTO.AddOrUpdateDTO addOrUpdateDTO);
}

