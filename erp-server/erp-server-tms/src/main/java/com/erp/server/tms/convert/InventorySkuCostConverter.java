package com.erp.server.tms.convert;

import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.dto.FirstMileSkuCostAllocationDetailDTO;
import com.erp.model.tms.dto.InitFirstMileAllocationDetailDTO;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;
import com.erp.model.tms.dto.excel.InitFirstMileAllocationDetailExcelDTO;
import com.erp.model.tms.dto.excel.InventorySkuCostDetailExcelDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.InventorySkuCostDetailEntity;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(uses = {TypeConversionWorker.class})
@Component
public interface InventorySkuCostConverter {
    InventorySkuCostConverter INSTANCE = Mappers.getMapper(InventorySkuCostConverter.class);

    @Mapping(target = "skuId", ignore = true)
    @Mapping(target = "remark", ignore = true )
    @Mapping(target = "mainId", ignore = true)
    InventorySkuCostDetailDTO.AddDTO excelToAddDTO(InventorySkuCostDetailExcelDTO excelDTO);
    @Mapping(target = "productCost", source = "productCost", qualifiedByName = "decimalToString")
    InventorySkuCostDetailDTO.ViewDTO detailToViewDTO(InventorySkuCostDetailEntity detailEntity);
    List<InventorySkuCostDetailDTO.ViewDTO> detailToViewDTO(List<InventorySkuCostDetailEntity> detailEntityList);

    InventorySkuCostDetailEntity addToDetail(InventorySkuCostDetailDTO.AddDTO detail);
    List<InventorySkuCostDetailEntity> addToDetail(List<InventorySkuCostDetailDTO.AddDTO> detailList);

    InventorySkuCostDetailEntity updateToDetail(InventorySkuCostDetailDTO.UpdateDTO detail);
    List<InventorySkuCostDetailEntity> updateToDetail(List<InventorySkuCostDetailDTO.UpdateDTO> detailList);
}
