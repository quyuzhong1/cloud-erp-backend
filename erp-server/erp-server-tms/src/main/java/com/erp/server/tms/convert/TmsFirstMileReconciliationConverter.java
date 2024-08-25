package com.erp.server.tms.convert;

import com.common.business.dto.base.BaseChildDTO;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 头程对账单
 */
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class})
public interface TmsFirstMileReconciliationConverter {

    TmsFirstMileReconciliationConverter INSTANCE = Mappers.getMapper(TmsFirstMileReconciliationConverter.class);


    @Mappings({
    })
    List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> convertDetailDTOList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceDTOList);


    @Mappings({
            @Mapping(target = "shippingCost", source = "shippingCost"),
            @Mapping(target = "declareCost", source = "declareCost"),
            @Mapping(target = "otherCost", source = "otherCost"),
            @Mapping(target = "otherTaxCost", source = "otherTaxCost"),
    })
    TmsFirstMileReconciliationDetailDTO.UpdateDTO convertDetailDTO(TmsFirstMileReconciliationDetailDTO.ListDTO sourceDTO);
}
