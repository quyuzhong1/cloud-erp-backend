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
import com.sdk.third.tf.dto.CreateCompanyDTO;
import com.sdk.third.tf.dto.EditCompanyDTO;
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

    /**
     * 实体类转换为新接口DTO（CfgInvoiceSettingEntity -> CreateCompanyDTO）
     * 对应新接口路径：/company/create
     */
    @Mappings({
            @Mapping(target = "invoiceType", source = "taxType"),  // 税务类型
            @Mapping(target = "name", source = "companyName"),  // 公司名称
            @Mapping(target = "cnpj", source = "leiCode"),  // CNPJ
            @Mapping(target = "ie", source = "stateTaxNo"),  // 州税号
            @Mapping(target = "unit", source = "dictCompanyType"),  // 公司类型（主公司或子公司）
            @Mapping(target = "email", source = "email"),  // 公司邮箱
            @Mapping(target = "cep", source = "postCode"),  // 邮编
            @Mapping(target = "address", source = "address"),  // 详细地址（原rua）
            @Mapping(target = "houseNumber", source = "doorplateNo"),  // 门牌号（原numero）
            @Mapping(target = "town", source = "district"),  // 区镇（原bairro）
            @Mapping(target = "city", source = "city"),  // 城市
            @Mapping(target = "state", source = "state"),  // 州
            @Mapping(target = "certFile", source = "certificateUrl"),  // 证书文件地址（原certificado）
            @Mapping(target = "certPwd", source = "certificatePassword"),  // 证书密码（原senhaCertificado）
            @Mapping(target = "serie", source = "no", qualifiedByName = "intToString"),  // 序列号（原numeroSerieNfe）
            @Mapping(target = "number", source = "startCode"),  // 起始编号（原ultimoNumeroNfe）
    })
    CreateCompanyDTO invoiceSettingToCreateCompanyDTO(CfgInvoiceSettingEntity entity);

    /**
     * 实体类转换为编辑公司DTO（CfgInvoiceSettingEntity -> EditCompanyDTO）
     * 对应新接口路径：/api/company/edit
     */
    @Mappings({
            @Mapping(target = "invoiceType", source = "taxType"),  // 税务类型
            @Mapping(target = "cnpj", source = "leiCode"),  // CNPJ
            @Mapping(target = "name", source = "companyName"),  // 公司名称
            @Mapping(target = "ie", source = "stateTaxNo"),  // 州税号
            @Mapping(target = "unit", source = "dictCompanyType"),  // 公司类型（主公司或子公司）
            @Mapping(target = "email", source = "email"),  // 公司邮箱
            @Mapping(target = "cep", source = "postCode"),  // 邮编
            @Mapping(target = "address", source = "address"),  // 详细地址
            @Mapping(target = "houseNumber", source = "doorplateNo"),  // 门牌号
            @Mapping(target = "town", source = "district"),  // 区镇
            @Mapping(target = "city", source = "city"),  // 城市
            @Mapping(target = "state", source = "state"),  // 州
            @Mapping(target = "certFile", source = "certificateUrl"),  // 证书文件地址
            @Mapping(target = "certPwd", source = "certificatePassword"),  // 证书密码
            @Mapping(target = "serie", source = "no"),  // 序列号（Integer类型）
            @Mapping(target = "number", source = "startCode", qualifiedByName = "stringToInt"),  // 起始编号（转换为Integer）
            @Mapping(target = "companyId", source = "companyId"),  // 公司ID（第三方返回的公司ID）
    })
    EditCompanyDTO invoiceSettingToEditCompanyDTO(CfgInvoiceSettingEntity entity);
}
