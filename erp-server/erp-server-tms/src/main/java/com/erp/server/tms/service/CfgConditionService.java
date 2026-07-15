package com.erp.server.tms.service;

import com.erp.model.tms.dto.CfgConditionDTO;
import com.erp.model.tms.entity.CfgConditionEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 条件配置表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-04-22
 */
public interface CfgConditionService extends SuperService<CfgConditionEntity> {
    /**
     * 新增规则条件
     * @param dto 规则条件
     */
    void add(CfgConditionDTO.AddDTO dto);
    /**
     * 编辑规则条件
     * @param dto 规则条件
     */
    void update(CfgConditionDTO.UpdateDTO dto);
    /**
     * 根据类型获取所有条件
     * @param type 类型
     */
    List<CfgConditionDTO.CommonDTO> listByType(String type);
    /**
     * 根据类型获取树结构
     * @param type 类型
     */
    List<CfgConditionDTO.TreeDTO> tree(String type);

    List<CfgConditionEntity> listByFields(List<String> fieldList);
}
