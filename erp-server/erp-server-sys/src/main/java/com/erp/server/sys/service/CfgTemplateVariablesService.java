package com.erp.server.sys.service;
import com.erp.model.sys.entity.CfgTemplateVariablesEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.CfgTemplateVariablesDTO;

import java.util.List;

/**
 * <p>
 * 模板字段表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-07-24
 */
public interface CfgTemplateVariablesService extends SuperService<CfgTemplateVariablesEntity> {


    List<CfgTemplateVariablesDTO.VariableGroupDTO> listByTemplateType(CfgTemplateVariablesDTO.TemplateParamDTO dto);
}
