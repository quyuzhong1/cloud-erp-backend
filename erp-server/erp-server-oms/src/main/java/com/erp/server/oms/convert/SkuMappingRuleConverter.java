package com.erp.server.oms.convert;

import com.erp.model.oms.dto.SkuMappingRuleDTO;
import com.erp.model.oms.entity.SkuMappingRuleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Mapper
@Component
public interface SkuMappingRuleConverter {
    SkuMappingRuleConverter INSTANCE = Mappers.getMapper(SkuMappingRuleConverter.class);

    @Mappings({
            @Mapping(target = "ruleTypeName", expression = "java(com.common.core.constant.EnumMessage.getNameByCode(com.erp.model.oms.enums.SkuMappingRuleEnum.class,skuMappingRuleEntity.getRuleType()))"),
    })
    SkuMappingRuleDTO.ListDTO entityToListDto(SkuMappingRuleEntity skuMappingRuleEntity);
    List<SkuMappingRuleDTO.ListDTO> entityToListDto(List<SkuMappingRuleEntity> skuMappingRuleEntity);

    @Mappings({
            @Mapping(target = "ruleTypeName", expression = "java(com.common.core.constant.EnumMessage.getNameByCode(com.erp.model.oms.enums.SkuMappingRuleEnum.class,skuMappingRuleEntity.getRuleType()))"),
    })
    SkuMappingRuleDTO.ViewDTO entityToViewDto(SkuMappingRuleEntity skuMappingRuleEntity);

    SkuMappingRuleDTO.CommonDTO entityToCommonDto(SkuMappingRuleEntity skuMappingRuleEntity);


    @Mappings({
            @Mapping(target = "ruleTypeName", expression = "java(com.common.core.constant.EnumMessage.getNameByCode(com.erp.model.oms.enums.SkuMappingRuleEnum.class,skuMappingRuleEntity.getRuleType()))")
    })
    SkuMappingRuleDTO.LogDTO entityToLogDTO(SkuMappingRuleEntity skuMappingRuleEntity);
}
