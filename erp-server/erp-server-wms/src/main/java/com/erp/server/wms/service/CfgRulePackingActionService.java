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
     * 保存仓位推荐动作
     * @param ruleId 规则主表id
     * @param actions 动作
     * @param ruleType 规则类型 RuleTypeEnum
     */
    void saveRuleAction(String ruleId, List<CfgRuleActionDTO.Add> actions, String ruleType);
    /**
     * 编辑仓位推荐动作
     * @param ruleId 规则主表id
     * @param actions 动作
     */
    void updateRuleAction(String ruleId, List<CfgRuleActionDTO.Update> actions, String ruleType);

    /**
     * 根据规则id获取拣货动作
     * @param cfgRuleIds 规则id
     * @param ruleType 规则类型
     */
    List<CfgRulePackingActionEntity> listByRuleIds(List<String> cfgRuleIds, String ruleType);

    /**
     * 按命中规则 + 动作库区优先级，查询 SKU 在对应仓位的可用库存。
     *
     * @param rules                命中的仓位推荐规则
     * @param warehouseIds         仓库范围
     * @param skuIds               SKU 范围
     * @param determiningCondition 库存数量过滤，如 gt；null 表示不过滤
     * @param ruleType             动作类型（拣货/补货/出库），用于过滤 cfg_rule_packing_action.rule_type
     * @return 仓位库存列表（按规则优先级、动作 index、数量降序）
     */
    List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> listLocationByRule(List<CfgRulePickingEntity> rules, List<String> warehouseIds, List<String> skuIds, String determiningCondition, String ruleType);
}
