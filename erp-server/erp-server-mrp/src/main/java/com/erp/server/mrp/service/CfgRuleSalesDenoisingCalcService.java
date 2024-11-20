package com.erp.server.mrp.service;

import com.common.business.service.SuperService;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingCalcEntity;

import java.util.List;

/**
 * <p>
 * 试算销量去噪信息 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CfgRuleSalesDenoisingCalcService extends SuperService<CfgRuleSalesDenoisingCalcEntity> {

    /**
     * 根据试算配置id查询销量公式
     *
     * @param cfgRuleCalcId 试算配置id
     */
    List<CfgRuleSalesDenoisingCalcEntity> listByCfgRuleCalcId(String cfgRuleCalcId);
    /**
     * 根据试算配置id查询销量公式
     *
     * @param cfgRuleCalcIds 试算id
     * @return
     */
    List<CfgRuleSalesDenoisingCalcEntity> listByCfgRuleCalcIds(List<String> cfgRuleCalcIds);
}
