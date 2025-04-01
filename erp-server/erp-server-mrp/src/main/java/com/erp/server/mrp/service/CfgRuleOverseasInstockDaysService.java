package com.erp.server.mrp.service;

import com.erp.model.mrp.dto.CfgRuleOverseasInstockDaysDTO;
import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
import com.erp.model.mrp.entity.CfgRuleOverseasInstockDaysEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 海外仓入库天数明细 服务类
 * </p>
 *
 * @author liaohui
 * @since 2025-02-13
 */
public interface CfgRuleOverseasInstockDaysService extends SuperService<CfgRuleOverseasInstockDaysEntity> {

    /**
     * 根据时效id查询海外仓入库天数明细
     * @param expireTimeIds 时效id
     */
    List<CfgRuleOverseasInstockDaysDTO.ViewDTO> listViewByExpireTimeIdList(List<String> expireTimeIds);

    /**
     * 更新海外仓入库配置
     * @param overseasInstockDaysList 海外仓入库天数
     * @param cfgRuleExpireTime  时效
     */
    void update(List<CfgRuleOverseasInstockDaysDTO.UpdateDTO> overseasInstockDaysList, CfgRuleExpireTimeEntity cfgRuleExpireTime);
}
