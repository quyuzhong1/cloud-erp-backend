package com.erp.server.oms.service;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RuleConditionDTO;

import java.util.List;

/**
 * <p>
 * 规则条件表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface RuleConditionService extends SuperService<RuleConditionEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(RuleConditionDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(RuleConditionDTO.UpdateDTO dto);


    /**
     * 保存规则条件
     * @author yl
     * @date 2023-08-31 15:47
     * @param ruleId
     * @param conditionList
     * @return void
     */
    void saveRuleCondition(String ruleId, List<RuleConditionDTO.AddDTO> conditionList);

    
    /**
     * 根据规则id 获取详情
     * @author yl
     * @date 2023-08-31 16:19
     * @param ruleId
     * @return java.util.List<com.erp.model.oms.dto.RuleConditionDTO.UpdateDTO>
     */
    List<RuleConditionDTO.ViewDTO> listByRuleId(String ruleId,String type);

    /**
     * 根据规则id集合 获取详情
     * @param ruleId
     * @param type
     * @return
     */
    List<RuleConditionDTO.ViewDTO> listByRuleIds(List<String> ruleId,String type);

    /**
     * 修改规则条件
     * @author yl
     * @date 2023-08-31 17:10
     * @param ruleId
     * @param conditionList
     * @return void
     */
    void updateRuleCondition(String ruleId, List<RuleConditionDTO.UpdateDTO> conditionList);


    /**
     * 根据规则id 集合获取到规则条件
     * @param ruleIdList
     * @return
     */
    List<RuleConditionEntity> listDbRuleIds(List<String> ruleIdList);
    /**
     * 根据规则id删除
     * @param ruleId
     */
    void removeByRuleId(String ruleId);
}
