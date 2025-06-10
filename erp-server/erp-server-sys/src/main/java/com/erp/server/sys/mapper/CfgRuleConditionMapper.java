package com.erp.server.sys.mapper;
import com.erp.model.sys.dto.CfgRuleConditionDTO;
import com.erp.model.sys.entity.CfgRuleConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 规则条件表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-05-23
 */
@Mapper
public interface CfgRuleConditionMapper extends BaseMapper<CfgRuleConditionEntity> {

    /**
     * 根据规则ids查询
     * @author will
     * @date 2024/6/26 10:04
     * @param cfgRuleIds
     * @return List<CfgRuleConditionEntity>
     */
    List<CfgRuleConditionDTO.ConditionElementDTO> listByRuleIds(@Param("cfgRuleIds") List<String> cfgRuleIds, @Param("ruleType")String ruleType);
    /**
     * 根据ruleType查询
     * @author jack
     * @date 2025-06-10
     * @param ruleType
     * @return List<CfgRuleConditionEntity>
     */
    List<CfgRuleConditionDTO.ConditionElementDTO> listByRuleType( @Param("ruleType")String ruleType);
}
