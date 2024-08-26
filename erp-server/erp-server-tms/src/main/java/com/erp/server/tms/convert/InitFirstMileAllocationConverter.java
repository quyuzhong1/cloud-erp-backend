package com.erp.server.tms.convert;

import com.erp.model.tms.dto.FirstMileSkuCostAllocationDetailDTO;
import com.erp.model.tms.dto.InitFirstMileAllocationDetailDTO;
import com.erp.model.tms.dto.excel.InitFirstMileAllocationDetailExcelDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(uses = {TypeConversionWorker.class})
@Component
public interface InitFirstMileAllocationConverter {
    InitFirstMileAllocationConverter INSTANCE = Mappers.getMapper(InitFirstMileAllocationConverter.class);

    @Mapping(target = "weightUnit", constant = "kg")
    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "sourceType", constant = "delivery")
    @Mapping(target = "sourceId", ignore = true)
    @Mapping(target = "sourceDetailId", ignore = true)
    @Mapping(target = "skuId", ignore = true)
    @Mapping(target = "shopId", ignore = true)
    @Mapping(target = "mainId", ignore = true)
    @Mapping(target = "exchangeRate", ignore = true)
    @Mapping(target = "currencySymbol", ignore = true)
    @Mapping(target = "businessType", ignore = true)
    InitFirstMileAllocationDetailDTO.AddDTO excelToAddDTO(InitFirstMileAllocationDetailExcelDTO excelDTO);
    @Mapping(target = "productCost", source = "productCost", qualifiedByName = "decimalToString")
    InitFirstMileAllocationDetailDTO.ViewDTO detailToViewDTO(InitFirstMileAllocationDetailEntity detailEntity);
    List<InitFirstMileAllocationDetailDTO.ViewDTO> detailToViewDTO(List<InitFirstMileAllocationDetailEntity> detailEntityList);
}
