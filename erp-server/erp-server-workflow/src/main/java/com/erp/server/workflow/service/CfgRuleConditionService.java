package com.erp.server.workflow.service;
import com.erp.model.workflow.entity.CfgRuleConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.workflow.dto.CfgRuleConditionDTO;

/**
 * <p>
 * 规则条件表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-12
 */
public interface CfgRuleConditionService extends SuperService<CfgRuleConditionEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgRuleConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-05-12
    * @param dto
    * @return
    */
    Boolean update(CfgRuleConditionDTO.UpdateDTO dto);


}
