package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleActionDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.entity.CfgRulePackingActionEntity;
import com.erp.model.wms.entity.CfgRulePickingEntity;

import java.util.List;

/**
 * <p>
 * 仓位分配规则表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
public interface CfgRulePackingActionService extends SuperService<CfgRulePackingActionEntity> {

    /**
     *  根据规则id删除拣货动作
     * @param ids 规则id
     */
    void removeByRuleIds(List<String> ids);

    /**
     * 保存拣货规则动作
     * @param ruleId 规则主表id
     * @param actions 动作
     */
    void saveRuleAction(String ruleId, List<CfgRuleActionDTO.Add> actions);
    /**
     * 编辑拣货规则动作
     * @param ruleId 规则主表id
     * @param actions 动作
     */
    void updateRuleAction(String ruleId, List<CfgRuleActionDTO.Update> actions);

    /**
     * 根据规则id获取拣货动作
     * @param cfgRuleIds 规则id
     */
    List<CfgRulePackingActionEntity> listByRuleIds(List<String> cfgRuleIds);

    List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> listLocationByRule(List<CfgRulePickingEntity> rules, List<String> warehouseIds, List<String> skuIds,String determiningCondition);
}
