package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.entity.CfgRuleConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 规则条件表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Mapper
public interface CfgRuleConditionMapper extends BaseMapper<CfgRuleConditionEntity> {

    List<CfgRuleConditionDTO.ConditionElementDTO> listByRuleIds(@Param("cfgRuleIds") List<String> cfgRuleIds);
}
