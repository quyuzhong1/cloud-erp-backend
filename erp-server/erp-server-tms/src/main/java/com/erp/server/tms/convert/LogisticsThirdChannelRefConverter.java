package com.erp.server.tms.convert;

import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.oms.dto.TransferDeclareProductDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDetailDTO;
import com.erp.model.tms.dto.excel.ImportLogisticsThirdChannelRefExcelDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
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
            @Mapping(target = "remark", constant = "excel导入"),
            @Mapping(target = "thirdChannelCode",  ignore = true)
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
