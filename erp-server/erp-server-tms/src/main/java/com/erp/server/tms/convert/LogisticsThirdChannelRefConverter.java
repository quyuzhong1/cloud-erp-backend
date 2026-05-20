package com.erp.server.tms.convert;

import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDetailDTO;
import com.erp.model.tms.dto.excel.ImportLogisticsThirdChannelRefExcelDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface LogisticsThirdChannelRefConverter {
    LogisticsThirdChannelRefConverter INSTANCE = Mappers.getMapper(LogisticsThirdChannelRefConverter.class);

    @Mappings({
    
            @Mapping(target = "detailList", ignore = true),
            @Mapping(target = "disabled", constant = "false"),
            @Mapping(target = "logisticsChannelCode",  ignore = true),
            @Mapping(target = "remark", constant = "excel 导入"),
            @Mapping(target = "thirdChannelCode",  ignore = true),
            @Mapping(target = "dictPlatform", source = "mainDictPlatform")
    })
    LogisticsThirdChannelRefDTO.AddDTO convertImportToAddDTO(ImportLogisticsThirdChannelRefExcelDTO importLogisticsThirdChannelRefExcelDTO);
    @Mappings({

            @Mapping(target = "disabled", constant = "false"),
            @Mapping(target = "mobile", source = "shopPhone"),
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "remark", ignore = true)
    })
    LogisticsThirdChannelRefDetailDTO.AddDTO convertImportToAddDetailDTO(ImportLogisticsThirdChannelRefExcelDTO excelDTO);
    List<LogisticsThirdChannelRefDetailDTO.AddDTO> convertImportToAddDetailDTO(List<ImportLogisticsThirdChannelRefExcelDTO> list);
}
