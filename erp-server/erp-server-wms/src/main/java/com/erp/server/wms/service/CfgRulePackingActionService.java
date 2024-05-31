package com.erp.server.wms.service;

import com.erp.model.wms.entity.CfgRulePackingActionEntity;
import com.common.business.service.SuperService;

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
}
