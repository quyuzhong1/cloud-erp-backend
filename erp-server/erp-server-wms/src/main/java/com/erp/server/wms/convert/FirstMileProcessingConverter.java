package com.erp.server.wms.convert;

import com.erp.model.wms.dto.FirstMileProcessingDTO;
import com.erp.model.wms.entity.FirstMileProcessingEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * 头程虚拟仓订单
 * @author will
 * @date 2025/1/3 9:10
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface FirstMileProcessingConverter {
    FirstMileProcessingConverter INSTANCE = Mappers.getMapper(FirstMileProcessingConverter.class);

    FirstMileProcessingDTO.AddOrUpdateDTO entityToAdd(FirstMileProcessingEntity entity);


    FirstMileProcessingEntity addToEntity(FirstMileProcessingDTO.AddOrUpdateDTO addOrUpdateDTO);
}
