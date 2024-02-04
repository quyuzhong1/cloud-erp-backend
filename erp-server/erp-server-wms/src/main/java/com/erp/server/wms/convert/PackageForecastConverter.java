package com.erp.server.wms.convert;

import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lambda
 * @Classname PackageForecastConverter
 * @Description TODO
 * @Date 2024-02-04 14:10
 * @Created by yl
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface PackageForecastConverter {
    PackageForecastConverter INSTANCE = Mappers.getMapper(PackageForecastConverter.class);


    @Mappings({
            @Mapping(target = "soId", source = "soId"),
            @Mapping(target = "soCode", source = "soCode"),
            @Mapping(target = "logisticsChannelId", source = "logisticsChannelId"),
            @Mapping(target = "trackNo", source = "transportNo"),
            @Mapping(target = "packageWeight", source = "weight"),
            @Mapping(target = "weightUnit", source = "weightUnit"),
    })
    TransferDeclareDetailDTO.AddDTO convertDeclareDetail(PackageForecastDetailEntity entity);
    List<TransferDeclareDetailDTO.AddDTO> convertDeclareDetail(List<PackageForecastDetailEntity> list);

}
