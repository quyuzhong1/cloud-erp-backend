package com.erp.server.mrp.service;

import com.erp.model.mrp.dto.CfgRuleSafeDaysDTO;
import com.erp.model.mrp.entity.CfgRuleSafeDaysEntity;
import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;

import java.util.List;

/**
 * <p>
 * 安全天数明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2025-02-17
 */
public interface CfgRuleSafeDaysService extends SuperService<CfgRuleSafeDaysEntity> {

    /**
     * 根据备货id查询安全天数
     * @param stockUpIdList 备货id
     */
    List<CfgRuleSafeDaysEntity> listByStockUpIdList(List<String> stockUpIdList);

    /**
     * 修改安全天数
     * @param safeDaysList 安全天数
     * @param cfgRuleStockUpEntity 备货
     * @param code 类型
     */
    void update(List<CfgRuleSafeDaysDTO.UpdateDTO> safeDaysList, CfgRuleStockUpEntity cfgRuleStockUpEntity, String code);
}
