package com.erp.server.sys.service;
import com.erp.model.sys.dto.CustomizeFieldLayoutDTO;
import com.erp.model.sys.entity.CfgQueryConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.CfgQueryConditionDTO;

import java.util.List;

/**
 * <p>
 * 查询条件配置表 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-01-03
 */
public interface CfgQueryConditionService extends SuperService<CfgQueryConditionEntity> {

    Boolean add(CfgQueryConditionDTO.AddDTO dto);

    List<CfgQueryConditionDTO.ViewDTO> getQueryCondition(String code);
}
