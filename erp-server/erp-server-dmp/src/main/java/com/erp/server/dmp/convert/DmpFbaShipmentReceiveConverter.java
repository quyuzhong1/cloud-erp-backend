package com.erp.server.dmp.convert;

import com.erp.model.dmp.lingxing.FbaReceiveDetailEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.sdk.third.lingxing.dto.FbaShipmentReceiveDTO;
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
public interface DmpFbaShipmentReceiveConverter {

    DmpFbaShipmentReceiveConverter INSTANCE = Mappers.getMapper(DmpFbaShipmentReceiveConverter.class);

    @Mappings({
            @Mapping(target = "receivedDateStr", expression = "java(dto.getReceivedDate().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd'T'HH:mm:ssXXX\")))"),
            @Mapping(target = "receivedDateLocaleStr", expression = "java(dto.getReceivedDateLocale().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd'T'HH:mm:ssXXX\")))"),
            @Mapping(target = "shopId", source = "shopId"),
    })
    FbaReceiveDetailEntity dtoToEntity(FbaShipmentReceiveDTO dto);

    @Mappings({
    })
    List<FbaReceiveDetailEntity> dtoListToEntityList(List<FbaShipmentReceiveDTO> dtoList);
}
