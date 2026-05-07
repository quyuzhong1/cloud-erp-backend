package com.erp.server.tms.service;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 报关规则主表 service
 */
public interface CfgDeclareRuleService extends SuperService<CfgDeclareRuleEntity> {

    BaseResultDTO.AddDTO add(CfgDeclareRuleDTO.AddDTO dto);

    Boolean add(CfgDeclareRuleDTO.SaveListDTO dto);

    Boolean update(CfgDeclareRuleDTO.UpdateDTO dto);

    CfgDeclareRuleDTO.SaveListDTO paging(CfgDeclareRuleDTO.ListParamDTO dto);

    List<BaseDropDownDTO.Tree> dropDownList(String type, String name);
}
