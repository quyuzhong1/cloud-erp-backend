package com.erp.server.sys.service;
import com.erp.model.sys.dto.CfgRuleConditionDTO;
import com.erp.model.sys.entity.CfgRuleConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;

import java.util.List;

/**
 * <p>
 * 规则条件表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-05-23
 */
public interface CfgRuleConditionService extends SuperService<CfgRuleConditionEntity> {

    /**
     *  根据规则id删除规则条件
     * @param ids 规则id
     */
    void removeByRuleIds(List<String> ids);

    /**
     * 保存规则条件
     *
     * @param ruleId        规则主表id
     * @param conditionList 条件
     * @param sourceType
     */
    void saveRuleCondition(String ruleId, List<CfgRuleConditionDTO.Add> conditionList, String sourceType);
    /**
     * 编辑规则条件
     *
     * @param ruleId        规则主表id
     * @param conditionList 条件
     */
    void updateRuleCondition(String ruleId, List<CfgRuleConditionDTO.Update> conditionList, String moduleType, String sourceType);

    /**
     * 根据规则id集合查询规则条件
     *
     * @param cfgRuleIds 规则id
     */
    List<CfgRuleConditionDTO.ConditionElementDTO> listByRuleIds(List<String> cfgRuleIds, String ruleType);

    List<CfgRuleConditionDTO.ConditionElementDTO> listByRuleType(String ruleType);
}
