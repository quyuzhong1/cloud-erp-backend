package com.erp.server.wms.mapper;

import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.entity.CfgRulePackingActionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 仓位分配规则表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Mapper
public interface CfgRulePackingActionMapper extends BaseMapper<CfgRulePackingActionEntity> {

    List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> listLocationByRule(@Param("ruleIds") List<String> ruleIds, @Param("warehouseIds") List<String> warehouseIds, @Param("skuIds") List<String> skuIds);
}
