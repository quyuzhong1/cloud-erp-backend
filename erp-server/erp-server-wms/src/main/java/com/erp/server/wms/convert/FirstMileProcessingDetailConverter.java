package com.erp.server.wms.convert;

import com.erp.model.wms.dto.FirstMileProcessingDetailDTO;
import com.erp.model.wms.entity.FirstMileProcessingDetailEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 头程虚拟仓订单
 * @author will
 * @date 2025/1/3 9:10
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface FirstMileProcessingDetailConverter {
    FirstMileProcessingDetailConverter INSTANCE = Mappers.getMapper(FirstMileProcessingDetailConverter.class);
    /**
     * 新增转实体
     */
    List<FirstMileProcessingDetailEntity> addToEntity(List<FirstMileProcessingDetailDTO.AddOrUpdateDTO> addOrUpdateList);
    /**
     * 实体转listDTO
     */
    FirstMileProcessingDetailDTO.ListDTO entityToList(FirstMileProcessingDetailEntity entity);

}
