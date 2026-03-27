package com.erp.server.tms.convert;

import com.erp.model.tms.dto.InventorySkuCostDetailDTO;
import com.erp.model.tms.dto.excel.InventorySkuCostDetailExcelDTO;
import com.erp.model.tms.entity.InventorySkuCostDetailEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(uses = {TypeConversionWorker.class})
@Component
public interface InventorySkuCostConverter {
    InventorySkuCostConverter INSTANCE = Mappers.getMapper(InventorySkuCostConverter.class);

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "skuId", ignore = true)
    @Mapping(target = "remark", ignore = true )
    @Mapping(target = "mainId", ignore = true)
    InventorySkuCostDetailDTO.AddDTO excelToAddDTO(InventorySkuCostDetailExcelDTO excelDTO);
    @Mapping(target = "productCost", source = "productCost", qualifiedByName = "decimalToString")
    InventorySkuCostDetailDTO.ViewDTO detailToViewDTO(InventorySkuCostDetailEntity detailEntity);
    List<InventorySkuCostDetailDTO.ViewDTO> detailToViewDTO(List<InventorySkuCostDetailEntity> detailEntityList);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    InventorySkuCostDetailEntity addToDetail(InventorySkuCostDetailDTO.AddDTO detail);
    List<InventorySkuCostDetailEntity> addToDetail(List<InventorySkuCostDetailDTO.AddDTO> detailList);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updateUserName", ignore = true)
    @Mapping(target = "updateUserId", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createUserName", ignore = true)
    @Mapping(target = "createUserId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    InventorySkuCostDetailEntity updateToDetail(InventorySkuCostDetailDTO.UpdateDTO detail);
    List<InventorySkuCostDetailEntity> updateToDetail(List<InventorySkuCostDetailDTO.UpdateDTO> detailList);
}
