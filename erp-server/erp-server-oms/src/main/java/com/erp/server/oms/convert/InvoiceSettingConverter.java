package com.erp.server.oms.convert;

/**
 * @description: InvoiceSettingToAddCompanyConverter
 * @author: hcg
 * @date: 2025/4/18 15:08
 */

import com.common.business.mapper.ObjectMapperWork;
import com.erp.model.oms.dto.CfgRuleInvoiceAmountDTO;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.sdk.third.tf.entity.AddCompanyDTO;
import com.sdk.third.tf.entity.UpdateCompanyDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: hcg
 * @CreateTime: 2025-04-18
 * @Description:
 * @Version: 1.0
 */
@Mapper(uses = {ObjectMapperWork.class})
@Component
public interface InvoiceSettingConverter {
    InvoiceSettingConverter INSTANCE = Mappers.getMapper(InvoiceSettingConverter.class);

    @Mappings({
            @Mapping(target = "razaoSocial", source = "companyName"),
            @Mapping(target = "cnpj", source = "leiCode"),
            @Mapping(target = "ie", source = "stateTaxNo"),
            @Mapping(target = "certificado", source = "certificateUrl"),
            @Mapping(target = "senhaCertificado", source = "certificatePassword"),
            @Mapping(target = "rua", source = "address"),
            @Mapping(target = "numero", source = "doorplateNo"),
            @Mapping(target = "bairro", source = "district"),
            @Mapping(target = "cep", source = "postCode"),
            @Mapping(target = "ultimoNumeroNfe", source = "startCode"),
            @Mapping(target = "numeroSerieNfe", source = "no"),
    })
    AddCompanyDTO invoiceSettinToAddCompanyDTOTo(CfgInvoiceSettingEntity entity);

    @Mappings({
            @Mapping(target = "razaoSocial", source = "companyName"),
            @Mapping(target = "cnpj", source = "leiCode"),
            @Mapping(target = "ie", source = "stateTaxNo"),
            @Mapping(target = "certificado", source = "certificateUrl"),
            @Mapping(target = "senhaCertificado", source = "certificatePassword"),
            @Mapping(target = "rua", source = "address"),
            @Mapping(target = "numero", source = "doorplateNo"),
            @Mapping(target = "bairro", source = "district"),
            @Mapping(target = "cep", source = "postCode"),
            @Mapping(target = "ultimoNumeroNfe", source = "startCode", qualifiedByName = "lastNumber"),
            @Mapping(target = "numeroSerieNfe", source = "no"),
            @Mapping(target = "tokenEmpresa", source = "token"),
    })
    UpdateCompanyDTO invoiceSettinToUpdateCompanyDTOTo(CfgInvoiceSettingEntity cfgVatInvoiceEntity);

    CfgRuleInvoiceAmountDTO.AddDTO toAddDTO(CfgRuleInvoiceAmountDTO.ViewDTO viewDTO);

    CfgRuleInvoiceAmountDTO.UpdateDTO toUpdateDTO(CfgRuleInvoiceAmountDTO.ViewDTO viewDTO);

    @Mapping(target = "name", source = "value")
    RuleConditionDTO.AddDTO conditionViewToAddDTO(RuleConditionDTO.ViewDTO condition);
    List<RuleConditionDTO.AddDTO> conditionViewToAddDTO(List<RuleConditionDTO.ViewDTO> conditionList);

    @Mapping(target = "name", source = "value")
    RuleConditionDTO.UpdateDTO conditionViewToUpdateDTO(RuleConditionDTO.ViewDTO condition);
    List<RuleConditionDTO.UpdateDTO> conditionViewToUpdateDTO(List<RuleConditionDTO.ViewDTO> conditionList);
}
