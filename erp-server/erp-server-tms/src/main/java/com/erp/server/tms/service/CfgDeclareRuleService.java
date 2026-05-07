package com.erp.server.tms.service;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;

import java.util.List;
import java.util.Map;

/**
 * 报关规则主表 service
 */
public interface CfgDeclareRuleService extends SuperService<CfgDeclareRuleEntity> {

    BaseResultDTO.AddDTO add(CfgDeclareRuleDTO.AddDTO dto);

    Boolean add(CfgDeclareRuleDTO.SaveListDTO dto);

    Boolean update(CfgDeclareRuleDTO.UpdateDTO dto);

    CfgDeclareRuleDTO.SaveListDTO paging(CfgDeclareRuleDTO.ListParamDTO dto);

    List<BaseDropDownDTO.Tree> dropDownList(String type, String name);

    /**
     * 根据规则类型和条件参数匹配报关规则。
     *
     * @param paramMap 参数集合，需包含 ruleType
     */
    List<CfgDeclareRuleEntity> listMatchedRule(Map<String, String> paramMap);
}
