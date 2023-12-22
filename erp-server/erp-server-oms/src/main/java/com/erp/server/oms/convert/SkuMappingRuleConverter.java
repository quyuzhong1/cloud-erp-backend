package com.erp.server.oms.convert;

import com.erp.model.oms.dto.SkuMappingRuleDTO;
import com.erp.model.oms.entity.SkuMappingRuleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface SkuMappingRuleConverter {
    SkuMappingRuleConverter INSTANCE = Mappers.getMapper(SkuMappingRuleConverter.class);

    SkuMappingRuleDTO.ListDTO entityToListDto(SkuMappingRuleEntity skuMappingRuleEntity);
    List<SkuMappingRuleDTO.ListDTO> entityToListDto(List<SkuMappingRuleEntity> skuMappingRuleEntity);

    SkuMappingRuleDTO.ViewDTO entityToViewDto(SkuMappingRuleEntity skuMappingRuleEntity);
}
