package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.RuleConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.RuleConditionDTO;

import java.util.List;

/**
 * <p>
 * 规则条件表 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-01-20
 */
public interface RuleConditionService extends SuperService<RuleConditionEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-01-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RuleConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-01-20
    * @param dto
    * @return
    */
    Boolean update(RuleConditionDTO.UpdateDTO dto);


    List<RuleConditionDTO.ViewDTO> listByRuleId(String id, String type);

    void saveRuleCondition(String id, List<RuleConditionDTO.AddDTO> conditionList);

    void updateRuleCondition(String id, List<RuleConditionDTO.UpdateDTO> conditionList);
}
