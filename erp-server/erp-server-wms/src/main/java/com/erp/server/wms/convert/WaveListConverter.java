package com.erp.server.wms.convert;

import com.erp.model.dmp.lingxing.FbaReceiveDetailEntity;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.entity.WaveListEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 领星FBA货件明细映射工具类
 * </p>
 *
 * @author Jim
 * @since 2024-02-18
 */
@Mapper
@Component
public interface WaveListConverter {

    WaveListConverter INSTANCE = Mappers.getMapper(WaveListConverter.class);

    WaveListDTO.ViewDTO entityToViewDTO(WaveListEntity record);
}
