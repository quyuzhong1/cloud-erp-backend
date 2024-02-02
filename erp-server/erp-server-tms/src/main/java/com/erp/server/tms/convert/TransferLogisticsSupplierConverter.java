package com.erp.server.tms.convert;

import com.common.business.dto.base.BaseChildDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @description: 物流商
 * @author Will
 * @date: 2023/11/17 16:36
 */
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class})
public interface TransferLogisticsSupplierConverter {

    TransferLogisticsSupplierConverter INSTANCE = Mappers.getMapper(TransferLogisticsSupplierConverter.class);

    @Mappings({
            @Mapping(target = "code", source = "id"),
            @Mapping(target = "value", source = "supplierName"),
            @Mapping(target = "disabled", source = "disabled"),

    })
    BaseDropDownDTO.DisabledDTO convertBySupplierDown(TransferLogisticsSupplierEntity logisticsChannel);
    List<BaseDropDownDTO.DisabledDTO> convertBySupplierDown(List<TransferLogisticsSupplierEntity> list);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "supplierName"),
            @Mapping(target = "disabled", source = "disabled"),

    })
    BaseChildDTO.ListChildTreeDTO convertTree(TransferLogisticsSupplierEntity entity);
    List<BaseChildDTO.ListChildTreeDTO> convertTree(List<TransferLogisticsSupplierEntity> dbList);
}
