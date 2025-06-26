package com.erp.server.scm.service;
import com.erp.model.scm.entity.CfgConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.CfgConditionDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-16
 */
public interface CfgConditionService extends SuperService<CfgConditionEntity> {
    List<CfgConditionDTO.CommonDTO> listByType(String sourceType);

    List<CfgConditionDTO.TreeDTO> tree(String type);
}
