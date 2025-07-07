package com.erp.server.scm.service;
import com.erp.model.scm.entity.CfgSupplierSalesConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.CfgSupplierSalesConditionDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 销量设置条件明细 服务类
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
public interface CfgSupplierSalesConditionService extends SuperService<CfgSupplierSalesConditionEntity> {


    void saveRuleCondition(String salesSettingId, List<CfgSupplierSalesConditionDTO.ConditionDTO> conditionList, String sourceType);

    void updateRuleCondition(String salesSettingId, List<CfgSupplierSalesConditionDTO.ConditionDTO> conditionList, String moduleType, String sourceType);
}
