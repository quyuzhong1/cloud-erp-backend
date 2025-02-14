package com.erp.server.mrp.service;

import com.erp.model.mrp.dto.CfgRuleExpireTimeDTO;
import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 时效配置表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2025-02-13
 */
public interface CfgRuleExpireTimeService extends SuperService<CfgRuleExpireTimeEntity> {

    /**
     * 修改
     */
    void update(CfgRuleExpireTimeDTO.UpdateDTO dto);
    /**
     * 查看详情
     */
    CfgRuleExpireTimeDTO.ViewDTO view();

}
