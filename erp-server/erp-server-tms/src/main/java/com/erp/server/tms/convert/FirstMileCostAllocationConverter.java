package com.erp.server.tms.convert;

import com.erp.model.tms.dto.FirstMileSkuCostAllocationDetailDTO;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface FirstMileCostAllocationConverter {
    FirstMileCostAllocationConverter INSTANCE = Mappers.getMapper(FirstMileCostAllocationConverter.class);

    FirstMileSkuCostAllocationDetailDTO.ViewDTO detailToViewDTO(FirstMileSkuCostAllocationDetailEntity detailEntity);
    List<FirstMileSkuCostAllocationDetailDTO.ViewDTO> detailToViewDTO(List<FirstMileSkuCostAllocationDetailEntity> detailEntityList1);
}
